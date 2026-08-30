package com.orginsight.service;

import java.util.List;

import com.orginsight.dto.common.PageResponse;
import com.orginsight.dto.request.KnowledgeItemRequest;
import com.orginsight.dto.response.KnowledgeItemResponse;
import com.orginsight.dto.response.KnowledgeStatisticsResponse;

public interface KnowledgeItemService {
    KnowledgeItemResponse create(KnowledgeItemRequest request);
    List<KnowledgeItemResponse> getAll();
    KnowledgeItemResponse getById(Long id);
    KnowledgeItemResponse view(Long id);
    KnowledgeItemResponse update(Long id, KnowledgeItemRequest request);
    void delete(Long id);

    PageResponse<KnowledgeItemResponse> search(String search, String category, String tag,
                                                int page, int size, String sortBy, String sortDir);

    KnowledgeStatisticsResponse getStatistics();
    List<String> getCategories();
}
