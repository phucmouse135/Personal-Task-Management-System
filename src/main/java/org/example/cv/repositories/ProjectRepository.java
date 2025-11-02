package org.example.cv.repositories;

import org.example.cv.models.entities.ProjectEntity;
import org.example.cv.utils.annotation.OwnableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
@OwnableRepository(entity = ProjectEntity.class)
public interface ProjectRepository extends BaseRepository<ProjectEntity, Long> {
    // findAll with filter by name or description
    @EntityGraph(attributePaths = {"owner"})
    @Query("SELECT p FROM ProjectEntity p WHERE " + "(LOWER(p.name) LIKE LOWER(CONCAT('%', :filter, '%')) OR "
            + "LOWER(p.description) LIKE LOWER(CONCAT('%', :filter, '%'))) AND "
            + "p.deletedAt IS NULL")
    Page<ProjectEntity> findAll(Pageable pageable, String filter);

    // findAll(pageable, filter, ownerId)
    @EntityGraph(attributePaths = {"owner"})
    @Query("SELECT p FROM ProjectEntity p WHERE " + "(LOWER(p.name) LIKE LOWER(CONCAT('%', :filter, '%')) OR "
            + "LOWER(p.description) LIKE LOWER(CONCAT('%', :filter, '%'))) AND "
            + "p.owner.id = :ownerId AND p.deletedAt IS NULL")
    Page<ProjectEntity> findAllByOwnerId(Pageable pageable, String filter, Long ownerId);

    // findById with owner fetch
    @EntityGraph(attributePaths = {"owner", "members"})
    java.util.Optional<ProjectEntity> findById(Long id);

    // findByOwnerIdOrMembersId - Tìm projects mà user là owner hoặc member
    @EntityGraph(attributePaths = {"owner", "members"})
    @Query("SELECT DISTINCT p FROM ProjectEntity p " + "LEFT JOIN p.members m "
            + "WHERE p.owner.id = :ownerId OR m.id = :memberId AND p.deletedAt IS NULL")
    Page<ProjectEntity> findByOwnerIdOrMembersId(Long ownerId, Long memberId, Pageable pageable);

    // findAllSoftDeletedByOwnerId
    @EntityGraph(attributePaths = {"owner"})
    @Query("SELECT p FROM ProjectEntity p WHERE p.deletedAt IS NOT NULL AND p.owner.id = :ownerId")
    Page<ProjectEntity> findAllSoftDeletedByOwnerId(Long ownerId, Pageable pageable);

    // findAllSoftDeleted - Tìm tất cả projects đã bị xóa mềm (cho admin)
    @EntityGraph(attributePaths = {"owner"})
    @Query("SELECT p FROM ProjectEntity p WHERE p.deletedAt IS NOT NULL")
    Page<ProjectEntity> findAllSoftDeleted(Pageable pageable);

    // findAllByMembersId - Tìm tất cả projects mà user là member
    @EntityGraph(attributePaths = {"owner", "members"})
    @Query("SELECT DISTINCT p FROM ProjectEntity p " + "LEFT JOIN p.members m "
            + "WHERE m.id = :memberId AND p.deletedAt IS NULL")
    java.util.List<ProjectEntity> findAllByMembersId(Long memberId);
}
