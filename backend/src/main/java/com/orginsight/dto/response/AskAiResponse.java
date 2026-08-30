package com.orginsight.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AskAiResponse {
    private String question;
    private String answer;
    private boolean aiGenerated;
    /** null when aiGenerated=true. Otherwise one of: NOT_CONFIGURED, RATE_LIMITED, AUTH_FAILURE, SERVER_ERROR, UNKNOWN */
    private String errorType;
}
