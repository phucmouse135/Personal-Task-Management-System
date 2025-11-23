package org.example.cv.repositories;

import java.util.Optional;

import org.example.cv.models.entities.TaskEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

@Repository
public interface TaskRepository extends BaseRepository<TaskEntity, Long> {
    @Query("SELECT t FROM TaskEntity t " + "LEFT JOIN FETCH t.project p "
            + "LEFT JOIN FETCH p.owner "
            + "LEFT JOIN FETCH t.assignees "
            + "WHERE t.id = :id")
    Optional<TaskEntity> findTaskWithDetailsById(@Param("id") Long id);

    Page<TaskEntity> findAllWithFilter(Specification<TaskEntity> spec, Pageable pageable);

    @Query(value = """
            SELECT DISTINCT t FROM TaskEntity t 
            LEFT JOIN FETCH t.project p 
            LEFT JOIN FETCH p.owner 
            LEFT JOIN t.assignees a 
            WHERE (a.id = :userId OR p.owner.id = :userId) 
            AND t.deletedAt IS NULL 
            ORDER BY t.deadline ASC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT t) FROM TaskEntity t 
            LEFT JOIN t.project p 
            LEFT JOIN t.assignees a 
            WHERE (a.id = :userId OR p.owner.id = :userId) 
            AND t.deletedAt IS NULL
            """)
    Page<TaskEntity> findByAssigneesIdOrProjectOwnerId(@Param("userId") Long userId, Pageable pageable);

    // Find all soft deleted tasks by owner
    @Query("SELECT DISTINCT t FROM TaskEntity t "
            + "LEFT JOIN FETCH t.project p "
            + "LEFT JOIN FETCH p.owner "
            + "WHERE p.owner.id = :ownerId "
            + "AND t.deletedAt IS NOT NULL "
            + "ORDER BY t.deletedAt DESC")
    Page<TaskEntity> findAllSoftDeletedByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    // Find all soft deleted tasks (admin)
    @Query("SELECT DISTINCT t FROM TaskEntity t "
            + "LEFT JOIN FETCH t.project p "
            + "LEFT JOIN FETCH p.owner "
            + "WHERE t.deletedAt IS NOT NULL "
            + "ORDER BY t.deletedAt DESC")
    Page<TaskEntity> findAllSoftDeleted(Pageable pageable);
}
