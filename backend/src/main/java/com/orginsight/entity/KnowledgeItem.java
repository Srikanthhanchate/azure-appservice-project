package com.orginsight.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "knowledge_items",
        indexes = {
                @Index(name = "idx_knowledge_category", columnList = "category"),
                @Index(name = "idx_knowledge_title", columnList = "title"),
                @Index(name = "idx_knowledge_department", columnList = "department"),
                @Index(name = "idx_knowledge_status", columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 4000)
    private String content;

    @Column(length = 100)
    private String category;

    @Column(length = 100)
    private String department;

    // Optional real link to a Project - lets the AI reason about which
    // projects have documentation coverage and which don't.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "author", length = 150)
    private String author;

    @Builder.Default
    @Column(length = 20)
    private String status = "Published";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "knowledge_item_tags", joinColumns = @JoinColumn(name = "knowledge_item_id"))
    @Column(name = "tag", length = 50)
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Builder.Default
    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @jakarta.persistence.PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @jakarta.persistence.PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
