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

    // findAllByPaginationAndSearch
    @Query("SELECT u FROM UserEntity u WHERE "
            + "(u.username LIKE %?1% OR u.email LIKE %?1% OR u.lastName LIKE %?1% OR u.firstName LIKE %?1%) "
            + "AND u.deletedAt IS NULL")
    @EntityGraph(attributePaths = {"roles"})
    Page<UserEntity> findAllByPaginationAndSearch(String search, Pageable pageable);

    // findByUsername
    @EntityGraph(attributePaths = {"roles"})
    Optional<UserEntity> findByUsername(String username);

    // findByEmail
    @EntityGraph(attributePaths = {"roles"})
    Optional<UserEntity> findByEmail(String email);
}
