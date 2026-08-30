package com.orginsight.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

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
public class KnowledgeItemResponse {
    private Long id;
    private String title;
    private String content;
    private String category;
    private String department;
    private Long projectId;
    private String projectName;
    private String author;
    private String status;
    private Set<String> tags;
    private String documentUrl;
    private String fileName;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
