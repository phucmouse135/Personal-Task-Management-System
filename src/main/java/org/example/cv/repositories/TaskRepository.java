package org.example.cv.repositories;

import java.util.Optional;

import org.example.cv.models.entities.TaskEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import feign.Param;

@Repository
public interface TaskRepository extends BaseRepository<TaskEntity, Long> {
    @Query("SELECT t FROM TaskEntity t " + "LEFT JOIN FETCH t.project p "
            + "LEFT JOIN FETCH p.owner "
            + "LEFT JOIN FETCH t.assignee "
            + "WHERE t.id = :id")
    Optional<TaskEntity> findTaskWithDetailsById(@Param("id") Long id);

    Page<TaskEntity> findAll(Specification<TaskEntity> spec, Pageable pageable);
}
