package com.orginsight.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

/**
 * Thin REST client for the Google Gemini API (generateContent endpoint).
 * Uses the JDK's built-in HttpClient so no extra Maven dependency is required.
 *
 * Docs: https://ai.google.dev/api/generate-content
 */
@Component
public class GeminiClient {

    private static final Logger logger = LoggerFactory.getLogger(GeminiClient.class);
    private static final String ENDPOINT_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    @Value("${gemini.enabled:true}")
    private boolean enabled;

    /**
     * Logs (once, at startup) which key/model is actually active, WITHOUT
     * exposing the secret - only its length and first/last few characters.
     * This exists specifically so you can verify which key is really being
     * used without grepping application.properties or guessing whether an
     * OS environment variable is silently overriding it (Spring Boot's
     * property precedence puts OS env vars ABOVE application.properties,
     * so a stale exported/set GEMINI_API_KEY will win over a new value you
     * just typed into the properties file).
     */
    @PostConstruct
    void logConfigurationOnStartup() {
        if (!enabled) {
            logger.info("Gemini AI is DISABLED (gemini.enabled=false).");
            return;
        }
        if (!StringUtils.hasText(apiKey)) {
            logger.warn("Gemini AI is ENABLED but no API key is configured (gemini.api-key is blank). "
                    + "AI Insights and Copilot will run in fallback mode.");
            return;
        }
        logger.info("Gemini AI configured: model={}, apiKey={} ({} chars) - "
                        + "if this doesn't match the key you just generated, check for a stale "
                        + "OS-level GEMINI_API_KEY environment variable overriding application.properties.",
                model, maskKey(apiKey), apiKey.length());

        if (!apiKey.startsWith("AIzaSy")) {
            logger.warn("The configured Gemini API key does NOT start with 'AIzaSy', which is the "
                    + "standard prefix for Gemini Developer API keys from Google AI Studio "
                    + "(https://aistudio.google.com/apikey). This looks like it may be the wrong "
                    + "type of credential (e.g. an OAuth token) rather than a Gemini API key, which "
                    + "commonly manifests as unexpected 401/403/429 errors regardless of quota.");
        }
    }

    private String maskKey(String key) {
        if (key.length() <= 8) {
            return "****";
        }
        return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
    }

    public boolean isConfigured() {
        return enabled && StringUtils.hasText(apiKey);
    }

    /**
     * Sends a prompt to Gemini and returns the model's plain-text answer.
     * Used for conversational Q&A (AI Copilot) where a natural-language
     * sentence is wanted rather than structured JSON.
     */
    public String generateText(String systemInstruction, String userPrompt) {
        String rawText = call(systemInstruction, userPrompt, false);
        return rawText.trim();
    }

    /**
     * Sends a prompt to Gemini and asks it to respond with raw JSON
     * (via response_mime_type=application/json) matching the given instructions.
     *
     * @param systemInstruction persona / output-format constraints
     * @param userPrompt        the actual context + question
     * @return the raw JSON text returned by the model
     */
    public String generateJson(String systemInstruction, String userPrompt) {
        return call(systemInstruction, userPrompt, true);
    }

    /**
     * Shared request/response handling for both generateText and generateJson -
     * avoids duplicating the HTTP plumbing in two near-identical methods.
     */
    private String call(String systemInstruction, String userPrompt, boolean jsonMode) {
        if (!isConfigured()) {
            throw new IllegalStateException("Gemini is not configured (missing API key or disabled).");
        }

        Map<String, Object> generationConfig = jsonMode
                ? Map.of("response_mime_type", "application/json", "temperature", 0.4, "maxOutputTokens", 1024)
                : Map.of("temperature", 0.5, "maxOutputTokens", 512);

        Map<String, Object> requestBody = Map.of(
                "system_instruction", Map.of("parts", new Object[]{Map.of("text", systemInstruction)}),
                "contents", new Object[]{
                        Map.of("role", "user", "parts", new Object[]{Map.of("text", userPrompt)})
                },
                "generationConfig", generationConfig
        );

        String url = String.format(ENDPOINT_TEMPLATE, model);
        String requestId = Integer.toHexString(System.identityHashCode(requestBody));

        logger.info("[gemini:{}] Sending request - model={}, promptChars={}, jsonMode={}",
                requestId, model, userPrompt.length(), jsonMode);

        HttpResponse<String> response;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .timeout(Duration.ofSeconds(25))
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            logger.error("[gemini:{}] Network/IO error calling Gemini: {}", requestId, e.getMessage());
            throw new IllegalStateException("Failed to reach the Gemini API: " + e.getMessage(), e);
        }

        logger.info("[gemini:{}] Received response - status={}, bodyChars={}",
                requestId, response.statusCode(), response.body() == null ? 0 : response.body().length());

        if (response.statusCode() != 200) {
            logger.warn("[gemini:{}] Non-200 response ({}): {}", requestId, response.statusCode(), response.body());
            throw new GeminiApiException(response.statusCode(), response.body());
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(response.body());
        } catch (Exception e) {
            logger.error("[gemini:{}] Could not parse Gemini response as JSON: {}", requestId, e.getMessage());
            throw new IllegalStateException("Gemini returned a response that could not be parsed.", e);
        }

        JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            logger.warn("[gemini:{}] Response had no usable text content. Full body: {}", requestId, response.body());
            throw new IllegalStateException("Gemini API returned an empty response.");
        }

        return textNode.asText();
    }
}
