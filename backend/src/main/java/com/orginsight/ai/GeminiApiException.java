package com.orginsight.ai;

/**
 * Thrown when a call to the Gemini API fails at the HTTP level.
 * Carries the actual status code so callers can distinguish between
 * quota exhaustion (429), auth failures (401/403), and upstream server
 * errors (5xx) instead of collapsing everything into one generic message.
 */
public class GeminiApiException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public GeminiApiException(int statusCode, String responseBody) {
        super("Gemini API call failed with status " + statusCode);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public boolean isRateLimited() {
        return statusCode == 429;
    }

    public boolean isAuthFailure() {
        return statusCode == 401 || statusCode == 403;
    }

    public boolean isServerError() {
        return statusCode >= 500;
    }
}
