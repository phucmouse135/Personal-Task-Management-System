package org.example.cv.repositories;

import java.util.Optional;

import org.example.cv.models.entities.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends BaseRepository<UserEntity, Long> {
    // existsByUsername
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserEntity u WHERE u.username = ?1")
    boolean existsByUsername(String username);

    // existsByEmail
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserEntity u WHERE u.email = ?1")
    boolean existsByEmail(String email);

    // findAllByPaginationAndSearch
    @Query(value = """
            SELECT * FROM users u 
            WHERE to_tsvector('english', username || ' ' || email || ' ' || first_name || ' ' || last_name)) @@ plainto_tsquery('english', ?1)
            AND u.deleted_at IS NULL
            """,
            countQuery = """
            SELECT COUNT(*) FROM users u 
            WHERE to_tsvector('english', username || ' ' || email || ' ' || first_name || ' ' || last_name)) @@ plainto_tsquery('english', ?1)
            AND u.deleted_at IS NULL
    """ , nativeQuery = true)
    Page<UserEntity> findAllByPaginationAndSearch(String search, Pageable pageable);

    @EntityGraph(attributePaths = {"roles"})
    Optional<UserEntity> findByUsername(String username);

    @EntityGraph(attributePaths = {"roles"})
    Optional<UserEntity> findByEmail(String email);
}
