package com.orginsight.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orginsight.ai.AiContextBuilder;
import com.orginsight.ai.GeminiApiException;
import com.orginsight.ai.GeminiClient;
import com.orginsight.dto.response.AskAiResponse;
import com.orginsight.service.AiCopilotService;

@Service
@Transactional(readOnly = true)
public class AiCopilotServiceImpl implements AiCopilotService {

    private static final Logger logger = LoggerFactory.getLogger(AiCopilotServiceImpl.class);

    private static final String SYSTEM_INSTRUCTION = """
            You are OrgInsight AI Copilot, an assistant embedded in an internal
            enterprise HR/project-management dashboard. You will be given an
            aggregated, anonymized snapshot of the organization's current data,
            followed by a question from an admin user. Answer the question in
            2-4 concise sentences, based strictly on the data provided. If the
            data doesn't contain enough information to answer confidently, say
            so plainly rather than guessing. Do not invent specific employee or
            project names - the data you're given is aggregated/anonymized.
            Do not use markdown formatting.
            """;

    private final GeminiClient geminiClient;
    private final AiContextBuilder contextBuilder;

    public AiCopilotServiceImpl(GeminiClient geminiClient, AiContextBuilder contextBuilder) {
        this.geminiClient = geminiClient;
        this.contextBuilder = contextBuilder;
    }

    @Override
    public AskAiResponse ask(String question) {
        logger.info("[copilot] Service received question (length={} chars)", question.length());

        if (!geminiClient.isConfigured()) {
            logger.info("[copilot] Gemini not configured - returning NOT_CONFIGURED response without calling the API.");
            return AskAiResponse.builder()
                    .question(question)
                    .answer("AI Copilot isn't configured yet. Set the GEMINI_API_KEY environment variable on the backend to enable it.")
                    .aiGenerated(false)
                    .errorType("NOT_CONFIGURED")
                    .build();
        }

        try {
            String context = contextBuilder.buildOrganizationSummary();
            String prompt = context + "\nQuestion: " + question;

            logger.info("[copilot] Calling Gemini (this is the ONLY Gemini call made for this request).");
            String answer = geminiClient.generateText(SYSTEM_INSTRUCTION, prompt);
            logger.info("[copilot] Gemini call succeeded, answer length={} chars", answer.length());

            return AskAiResponse.builder()
                    .question(question)
                    .answer(answer)
                    .aiGenerated(true)
                    .build();

        } catch (GeminiApiException e) {
            String errorType;
            String userMessage;

            if (e.isRateLimited()) {
                errorType = "RATE_LIMITED";
                userMessage = "The AI service's request quota has been used up for now (HTTP 429). "
                        + "This resets over time - see the backend logs for the exact Gemini response, "
                        + "and double-check the configured API key is a genuine key from "
                        + "https://aistudio.google.com/apikey (starts with \"AIzaSy\").";
                logger.warn("[copilot] Gemini rate-limited (429): {}", e.getResponseBody());
            } else if (e.isAuthFailure()) {
                errorType = "AUTH_FAILURE";
                userMessage = "The AI service rejected the configured API key (HTTP " + e.getStatusCode()
                        + "). Verify GEMINI_API_KEY is a valid, current Gemini API key.";
                logger.warn("[copilot] Gemini auth failure ({}): {}", e.getStatusCode(), e.getResponseBody());
            } else if (e.isServerError()) {
                errorType = "SERVER_ERROR";
                userMessage = "The AI service is temporarily unavailable (HTTP " + e.getStatusCode() + "). Please try again shortly.";
                logger.warn("[copilot] Gemini server error ({}): {}", e.getStatusCode(), e.getResponseBody());
            } else {
                errorType = "UNKNOWN";
                userMessage = "The AI service returned an unexpected error (HTTP " + e.getStatusCode() + ").";
                logger.warn("[copilot] Gemini unexpected status ({}): {}", e.getStatusCode(), e.getResponseBody());
            }

            return AskAiResponse.builder()
                    .question(question)
                    .answer(userMessage)
                    .aiGenerated(false)
                    .errorType(errorType)
                    .build();

        } catch (Exception e) {
            logger.warn("[copilot] Non-HTTP failure calling Gemini: {}", e.getMessage());
            return AskAiResponse.builder()
                    .question(question)
                    .answer("I couldn't reach the AI service just now. Please try again in a moment.")
                    .aiGenerated(false)
                    .errorType("UNKNOWN")
                    .build();
        }
    }
}
