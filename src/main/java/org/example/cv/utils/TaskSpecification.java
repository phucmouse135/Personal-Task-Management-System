package org.example.cv.utils;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

import org.example.cv.models.entities.TaskEntity;
import org.example.cv.models.requests.TaskFilterRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

public class TaskSpecification {

    public static Specification<TaskEntity> fromFilter(TaskFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.projectId() != null) {
                predicates.add(cb.equal(root.get("project").get("id"), filter.projectId()));
            }
            if (filter.assigneeId() != null) {
                predicates.add(
                        cb.isMember(filter.assigneeId(), root.join("assignees").get("id")));
            }
            if (!CollectionUtils.isEmpty(filter.statuses())) {
                predicates.add(root.get("status").in(filter.statuses()));
            }
            if (!CollectionUtils.isEmpty(filter.priorities())) {
                predicates.add(root.get("priority").in(filter.priorities()));
            }
            if (filter.deadlineFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("deadline"), filter.deadlineFrom()));
            }
            if (filter.deadlineTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("deadline"), filter.deadlineTo().plus(1, ChronoUnit.DAYS)));
            }

            // Tránh N+1 query - chỉ fetch khi query trả về entity (không phải count query)
            if (query.getResultType().equals(TaskEntity.class)) {
                query.distinct(true);
                root.fetch("project");
                root.fetch("assignees");
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
