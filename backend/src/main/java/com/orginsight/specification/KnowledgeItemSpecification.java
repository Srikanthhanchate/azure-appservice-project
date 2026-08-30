package com.orginsight.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.orginsight.entity.KnowledgeItem;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public final class KnowledgeItemSpecification {

    private KnowledgeItemSpecification() {
    }

    public static Specification<KnowledgeItem> filterBy(String search, String category, String tag) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query != null) {
                query.distinct(true);
            }

            if (StringUtils.hasText(search)) {
                String like = "%" + search.toLowerCase() + "%";
                Predicate byTitle = cb.like(cb.lower(root.get("title")), like);
                Predicate byContent = cb.like(cb.lower(root.get("content")), like);
                predicates.add(cb.or(byTitle, byContent));
            }
            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
            }
            if (StringUtils.hasText(tag)) {
                predicates.add(cb.isMember(tag, root.get("tags")));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
