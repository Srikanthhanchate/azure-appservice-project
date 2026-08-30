package com.orginsight.dto.response;

import java.util.List;

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
public class AiInsightResponse {
    private int healthScore;
    private String healthStatus;
    private List<String> recommendations;
    private List<String> alerts;
    private List<String> skillGaps;
    private String workloadSummary;
    private String note;
}
