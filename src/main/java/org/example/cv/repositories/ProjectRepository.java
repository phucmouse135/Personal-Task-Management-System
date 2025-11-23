package org.example.cv.repositories;

import java.util.List;
import java.util.Optional;

import org.example.cv.models.entities.ProjectEntity;
import org.example.cv.utils.annotation.OwnableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@OwnableRepository(entity = ProjectEntity.class)
public interface ProjectRepository extends BaseRepository<ProjectEntity, Long> {

    @Query(value = """
            SELECT p FROM projects p 
            WHERE to_tsvector('english', p.name || ' ' || coalesce(p.description, '')) @@ plainto_tsquery('english', :filter)
            AND p.deleted_at IS NULL
            """,
            countQuery = """
            SELECT COUNT(*) FROM projects p 
            WHERE to_tsvector('english', p.name || ' ' || coalesce(p.description, '')) @@ plainto_tsquery('english', :filter)
            AND p.deleted_at IS NULL
            """,
            nativeQuery = true)
    Page<ProjectEntity> findAll(Pageable pageable, @Param("filter") String filter);

    @Query(value = """
        SELECT * FROM projects p 
        WHERE to_tsvector('english', p.name || ' ' || coalesce(p.description , '')) @@ plainto_tsquery('english', :filter)
        AND p.owner_id = :ownerId 
        AND p.deleted_at IS NULL
    """,
            countQuery = """
        SELECT COUNT(*) FROM projects p 
        WHERE to_tsvector('english', p.name || ' ' || coalesce(p.description , '')) @@ plainto_tsquery('english', :filter)
        AND p.owner_id = :ownerId 
        AND p.deleted_at IS NULL
    """, nativeQuery = true
    )
    Page<ProjectEntity> findAllByOwnerId(Pageable pageable, @Param("filter") String filter, @Param("ownerId") Long ownerId);

    @EntityGraph(attributePaths = {"owner", "members"})
    Optional<ProjectEntity> findById(Long id);


    @Query(
            value = """
            SELECT DISTINCT p FROM ProjectEntity p 
            LEFT JOIN p.members m 
            WHERE (p.owner.id = :userId OR m.id = :userId) 
              AND p.deletedAt IS NULL
        """,
            countQuery = """
            SELECT COUNT(DISTINCT p) FROM ProjectEntity p 
            LEFT JOIN p.members m 
            WHERE (p.owner.id = :userId OR m.id = :userId) 
              AND p.deletedAt IS NULL
        """
    )
    Page<ProjectEntity> findByOwnerIdOrMembersId(@Param("userId") Long userId, Long currentUserId, Pageable pageable);
    // Note: Gom ownerId và memberId thành 1 biến userId vì logic thường là check cho cùng 1 người.

    // 5. SOFT DELETED
    @EntityGraph(attributePaths = {"owner"})
    @Query("SELECT p FROM ProjectEntity p WHERE p.deletedAt IS NOT NULL AND p.owner.id = :ownerId")
    Page<ProjectEntity> findAllSoftDeletedByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    @EntityGraph(attributePaths = {"owner"})
    @Query("SELECT p FROM ProjectEntity p WHERE p.deletedAt IS NOT NULL")
    Page<ProjectEntity> findAllSoftDeleted(Pageable pageable);


    @Query("""
        SELECT DISTINCT p FROM ProjectEntity p 
        LEFT JOIN p.members m 
        WHERE m.id = :memberId AND p.deletedAt IS NULL
    """)
    List<ProjectEntity> findAllByMembersId(@Param("memberId") Long memberId);
}