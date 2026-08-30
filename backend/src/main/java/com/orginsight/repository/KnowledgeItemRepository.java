package com.orginsight.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.orginsight.entity.KnowledgeItem;

import java.util.List;

@Repository
public interface KnowledgeItemRepository extends JpaRepository<KnowledgeItem, Long>, JpaSpecificationExecutor<KnowledgeItem> {
    long countByCategory(String category);

    @org.springframework.data.jpa.repository.Query("select distinct k.category from KnowledgeItem k where k.category is not null and k.category <> '' order by k.category")
    List<String> findDistinctCategories();
}
