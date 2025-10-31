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
    @Query("SELECT p FROM ProjectEntity p WHERE " + "LOWER(p.name) LIKE LOWER(CONCAT('%', :filter, '%')) OR "
            + "LOWER(p.description) LIKE LOWER(CONCAT('%', :filter, '%'))")
    Page<ProjectEntity> findAll(Pageable pageable, String filter);

    // findAll(pageable, filter, ownerId)
    @EntityGraph(attributePaths = {"owner"})
    @Query("SELECT p FROM ProjectEntity p WHERE " + "(LOWER(p.name) LIKE LOWER(CONCAT('%', :filter, '%')) OR "
            + "LOWER(p.description) LIKE LOWER(CONCAT('%', :filter, '%'))) AND "
            + "p.owner.id = :ownerId")
    Page<ProjectEntity> findAllByOwnerId(Pageable pageable, String filter, Long ownerId);

    // findById with owner fetch
    @EntityGraph(attributePaths = {"owner", "members"})
    java.util.Optional<ProjectEntity> findById(Long id);
}
