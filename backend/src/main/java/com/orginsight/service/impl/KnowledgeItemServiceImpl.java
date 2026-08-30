package com.orginsight.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orginsight.dto.common.PageResponse;
import com.orginsight.dto.request.KnowledgeItemRequest;
import com.orginsight.dto.response.KnowledgeItemResponse;
import com.orginsight.dto.response.KnowledgeStatisticsResponse;
import com.orginsight.entity.KnowledgeItem;
import com.orginsight.entity.Project;
import com.orginsight.exception.KnowledgeItemNotFoundException;
import com.orginsight.repository.KnowledgeItemRepository;
import com.orginsight.repository.ProjectRepository;
import com.orginsight.service.KnowledgeItemService;
import com.orginsight.specification.KnowledgeItemSpecification;

@Service
@Transactional
public class KnowledgeItemServiceImpl implements KnowledgeItemService {

    private final KnowledgeItemRepository repository;
    private final ProjectRepository projectRepository;

    public KnowledgeItemServiceImpl(KnowledgeItemRepository repository, ProjectRepository projectRepository) {
        this.repository = repository;
        this.projectRepository = projectRepository;
    }

    @Override
    public KnowledgeItemResponse create(KnowledgeItemRequest request) {
        KnowledgeItem item = new KnowledgeItem();
        mapRequestToEntity(request, item);
        return mapToResponse(repository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public List<KnowledgeItemResponse> getAll() {
        return repository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeItemResponse getById(Long id) {
        KnowledgeItem item = repository.findById(id)
                .orElseThrow(() -> new KnowledgeItemNotFoundException("Knowledge item not found with id " + id));
        return mapToResponse(item);
    }

    @Override
    public KnowledgeItemResponse view(Long id) {
        KnowledgeItem item = repository.findById(id)
                .orElseThrow(() -> new KnowledgeItemNotFoundException("Knowledge item not found with id " + id));
        item.setViewCount(item.getViewCount() == null ? 1 : item.getViewCount() + 1);
        return mapToResponse(repository.save(item));
    }

    @Override
    public KnowledgeItemResponse update(Long id, KnowledgeItemRequest request) {
        KnowledgeItem item = repository.findById(id)
                .orElseThrow(() -> new KnowledgeItemNotFoundException("Knowledge item not found with id " + id));
        mapRequestToEntity(request, item);
        return mapToResponse(repository.save(item));
    }

    @Override
    public void delete(Long id) {
        KnowledgeItem item = repository.findById(id)
                .orElseThrow(() -> new KnowledgeItemNotFoundException("Knowledge item not found with id " + id));
        repository.delete(item);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<KnowledgeItemResponse> search(String search, String category, String tag,
                                                        int page, int size, String sortBy, String sortDir) {
        String sortField = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(direction, sortField));

        Page<KnowledgeItem> result = repository.findAll(
                KnowledgeItemSpecification.filterBy(search, category, tag), pageable);

        return PageResponse.from(result.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeStatisticsResponse getStatistics() {
        List<KnowledgeItem> all = repository.findAll();
        Map<String, Long> byCategory = all.stream()
                .filter(k -> k.getCategory() != null && !k.getCategory().isBlank())
                .collect(Collectors.groupingBy(KnowledgeItem::getCategory, Collectors.counting()));
        long totalViews = all.stream().mapToLong(k -> k.getViewCount() == null ? 0 : k.getViewCount()).sum();

        return KnowledgeStatisticsResponse.builder()
                .totalItems(all.size())
                .byCategory(byCategory)
                .totalViews(totalViews)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return repository.findDistinctCategories();
    }

    private void mapRequestToEntity(KnowledgeItemRequest request, KnowledgeItem item) {
        item.setTitle(request.getTitle());
        item.setContent(request.getContent());
        item.setCategory(request.getCategory());
        item.setDepartment(request.getDepartment());
        item.setAuthor(request.getAuthor());
        item.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? "Published" : request.getStatus());
        item.setTags(request.getTags() == null ? new HashSet<>() : new HashSet<>(request.getTags()));
        item.setDocumentUrl(request.getDocumentUrl());
        item.setFileName(request.getFileName());

        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId()).orElse(null);
            item.setProject(project);
        } else {
            item.setProject(null);
        }
    }

    private KnowledgeItemResponse mapToResponse(KnowledgeItem item) {
        return KnowledgeItemResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .content(item.getContent())
                .category(item.getCategory())
                .department(item.getDepartment())
                .projectId(item.getProject() != null ? item.getProject().getId() : null)
                .projectName(item.getProject() != null ? item.getProject().getName() : null)
                .author(item.getAuthor())
                .status(item.getStatus())
                .tags(item.getTags())
                .documentUrl(item.getDocumentUrl())
                .fileName(item.getFileName())
                .viewCount(item.getViewCount())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
