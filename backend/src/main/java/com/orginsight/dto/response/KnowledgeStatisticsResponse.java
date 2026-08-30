package com.orginsight.dto.response;

import java.util.Map;

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
public class KnowledgeStatisticsResponse {
    private long totalItems;
    private Map<String, Long> byCategory;
    private long totalViews;
}
