package com.orginsight.service.impl;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orginsight.ai.AiContextBuilder;
import com.orginsight.ai.GeminiApiException;
import com.orginsight.ai.GeminiClient;
import com.orginsight.dto.response.AiInsightResponse;
import com.orginsight.repository.EmployeeRepository;
import com.orginsight.repository.ProjectRepository;
import com.orginsight.service.AiInsightsService;

/**
 * AI insights service.
 *
 * If Gemini is configured (see gemini.* properties), real insights are
 * generated from a Gemini call using an aggregated, PII-free summary of the
 * organization's data as context, then cached for gemini.cache-minutes.
 *
 * If Gemini is not configured, or the call fails for any reason (missing
 * key, network error, rate limit, malformed response), this falls back to a
 * transparent, static/derived heuristic computed directly from the database
 * so the AI Insights screen never breaks.
 */
@Service
@Transactional(readOnly = true)
public class AiInsightsServiceImpl implements AiInsightsService {

    private static final Logger logger = LoggerFactory.getLogger(AiInsightsServiceImpl.class);

    private static final String SYSTEM_INSTRUCTION = """
            You are an HR and project-management analytics assistant embedded in an
            internal enterprise dashboard called OrgInsight AI. You will be given an
            aggregated, anonymized snapshot of an organization's employee and project
            data. Respond with ONLY a single JSON object (no markdown, no commentary)
            with exactly these fields:
            {
              "healthScore": <integer 0-100, overall organizational health>,
              "healthStatus": <one of "Excellent","Good","Fair","Needs Attention">,
              "recommendations": [<3 short, specific, actionable string recommendations>],
              "alerts": [<0-3 short strings flagging concrete risks found in the data>],
              "skillGaps": [<2-4 short strings naming likely skill gaps inferable from the data>],
              "workloadSummary": <one short sentence describing workforce workload balance>
            }
            Base every field strictly on the provided data. Do not invent employee or
            project names - the data given to you is already anonymized/aggregated.
            """;

    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final GeminiClient geminiClient;
    private final AiContextBuilder contextBuilder;
    private final ObjectMapper objectMapper;

    @Value("${gemini.cache-minutes:60}")
    private long cacheMinutes;

    private volatile AiInsightResponse cachedResponse;
    private volatile Instant cachedAt;

    public AiInsightsServiceImpl(EmployeeRepository employeeRepository,
                                  ProjectRepository projectRepository,
                                  GeminiClient geminiClient,
                                  AiContextBuilder contextBuilder,
                                  ObjectMapper objectMapper) {
        this.employeeRepository = employeeRepository;
        this.projectRepository = projectRepository;
        this.geminiClient = geminiClient;
        this.contextBuilder = contextBuilder;
        this.objectMapper = objectMapper;
    }

    @Override
    public AiInsightResponse getInsights() {
        if (isCacheValid()) {
            logger.info("[insights] Returning cached AI insights (age < {} min) - no Gemini call made.", cacheMinutes);
            return cachedResponse;
        }

        if (geminiClient.isConfigured()) {
            try {
                logger.info("[insights] Cache miss/expired - calling Gemini (this is the ONLY Gemini call made for this request).");
                AiInsightResponse aiResponse = generateWithGemini();
                cachedResponse = aiResponse;
                cachedAt = Instant.now();
                logger.info("[insights] Gemini call succeeded, response cached for {} min.", cacheMinutes);
                return aiResponse;
            } catch (GeminiApiException e) {
                if (e.isRateLimited()) {
                    logger.warn("[insights] Gemini rate-limited (429), falling back to static insights: {}", e.getResponseBody());
                } else if (e.isAuthFailure()) {
                    logger.warn("[insights] Gemini auth failure ({}), falling back to static insights: {}", e.getStatusCode(), e.getResponseBody());
                } else {
                    logger.warn("[insights] Gemini returned {}, falling back to static insights: {}", e.getStatusCode(), e.getResponseBody());
                }
            } catch (Exception e) {
                logger.warn("[insights] Gemini AI insights generation failed, falling back to static insights: {}", e.getMessage());
            }
        } else {
            logger.info("[insights] Gemini not configured - using static insights.");
        }

        AiInsightResponse fallback = buildFallbackInsights();
        // Fallback results are cheap to compute, so we don't cache them -
        // every request retries Gemini (in case the earlier failure was transient).
        return fallback;
    }

    private boolean isCacheValid() {
        return cachedResponse != null && cachedAt != null
                && cachedAt.plusSeconds(cacheMinutes * 60).isAfter(Instant.now());
    }

    private AiInsightResponse generateWithGemini() throws Exception {
        String context = contextBuilder.buildOrganizationSummary();
        String prompt = context + "\nGenerate the JSON insights object described in your instructions.";

        String rawJson = geminiClient.generateJson(SYSTEM_INSTRUCTION, prompt);
        JsonNode node = objectMapper.readTree(rawJson);

        return AiInsightResponse.builder()
                .healthScore(node.path("healthScore").asInt(70))
                .healthStatus(node.path("healthStatus").asText("Good"))
                .recommendations(toStringList(node.path("recommendations")))
                .alerts(toStringList(node.path("alerts")))
                .skillGaps(toStringList(node.path("skillGaps")))
                .workloadSummary(node.path("workloadSummary").asText(""))
                .note("Generated by Google Gemini from live organization data.")
                .build();
    }

    private List<String> toStringList(JsonNode arrayNode) {
        if (!arrayNode.isArray()) {
            return List.of();
        }
        return java.util.stream.StreamSupport.stream(arrayNode.spliterator(), false)
                .map(JsonNode::asText)
                .toList();
    }

    private AiInsightResponse buildFallbackInsights() {
        long totalEmployees = employeeRepository.count();
        long activeEmployees = employeeRepository.countByStatus("ACTIVE");
        long totalProjects = projectRepository.count();
        long completedProjects = projectRepository.countByStatus("COMPLETED");

        int healthScore = 70;
        if (totalEmployees > 0) {
            healthScore += (int) Math.min(20, (activeEmployees * 20) / Math.max(totalEmployees, 1));
        }
        if (totalProjects > 0) {
            healthScore += (int) Math.min(10, (completedProjects * 10) / Math.max(totalProjects, 1));
        }
        healthScore = Math.min(healthScore, 100);

        String healthStatus = healthScore >= 85 ? "Excellent" : healthScore >= 70 ? "Good" : healthScore >= 50 ? "Fair" : "Needs Attention";

        return AiInsightResponse.builder()
                .healthScore(healthScore)
                .healthStatus(healthStatus)
                .recommendations(List.of(
                        "Review employee workload distribution across departments.",
                        "Prioritize projects nearing their end date with low progress.",
                        "Encourage knowledge base contributions from senior staff."
                ))
                .alerts(List.of(
                        totalProjects == 0 ? "No projects have been created yet." : "Monitor projects with below-average progress.",
                        totalEmployees == 0 ? "No employees have been added yet." : "Verify department assignments are up to date."
                ))
                .skillGaps(List.of(
                        "Cloud infrastructure",
                        "Data analytics",
                        "Agile project management"
                ))
                .workloadSummary("Workload distribution appears " + (activeEmployees > 0 ? "balanced across active staff." : "unavailable due to insufficient data."))
                .note("Static, rule-based summary (AI insights unavailable - check gemini.api-key / gemini.enabled).")
                .build();
    }
}
