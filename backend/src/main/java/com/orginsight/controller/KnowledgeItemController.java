package com.orginsight.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.orginsight.dto.common.ApiResponse;
import com.orginsight.dto.common.PageResponse;
import com.orginsight.dto.request.KnowledgeItemRequest;
import com.orginsight.dto.response.KnowledgeItemResponse;
import com.orginsight.dto.response.KnowledgeStatisticsResponse;
import com.orginsight.service.KnowledgeItemService;
import com.orginsight.storage.FileStorageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeItemController {

    private final KnowledgeItemService knowledgeItemService;
    private final FileStorageService fileStorageService;

    public KnowledgeItemController(KnowledgeItemService knowledgeItemService, FileStorageService fileStorageService) {
        this.knowledgeItemService = knowledgeItemService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping
    public ResponseEntity<KnowledgeItemResponse> create(@Valid @RequestBody KnowledgeItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(knowledgeItemService.create(request));
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadDocument(@RequestPart("file") MultipartFile file) {
        try {
            FileStorageService.StoredFile stored = fileStorageService.store(file);
            return ResponseEntity.ok(Map.of("fileName", stored.originalFileName(), "documentUrl", stored.url()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to store uploaded file", e);
        }
    }

    @GetMapping
    public ResponseEntity<List<KnowledgeItemResponse>> getAll() {
        return ResponseEntity.ok(knowledgeItemService.getAll());
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<KnowledgeItemResponse>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(knowledgeItemService.search(search, category, tag, page, size, sortBy, sortDir));
    }

    @GetMapping("/statistics")
    public ResponseEntity<KnowledgeStatisticsResponse> getStatistics() {
        return ResponseEntity.ok(knowledgeItemService.getStatistics());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(knowledgeItemService.getCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<KnowledgeItemResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(knowledgeItemService.getById(id));
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<KnowledgeItemResponse> view(@PathVariable Long id) {
        return ResponseEntity.ok(knowledgeItemService.view(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<KnowledgeItemResponse> update(@PathVariable Long id, @Valid @RequestBody KnowledgeItemRequest request) {
        return ResponseEntity.ok(knowledgeItemService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        knowledgeItemService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Knowledge item deleted successfully", null));
    }
}
