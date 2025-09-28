package org.example.cv.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.example.cv.models.entities.RoleEntity;
import org.example.cv.models.entities.UserEntity;
import org.example.cv.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    private UserEntity user;
    private RoleEntity role;

    @BeforeEach
    void setUp() {
        role = RoleEntity.builder()
                .name("ADMIN")
                .description("Administrator role")
                .build();

        user = UserEntity.builder()
                .id(1L)
                .username("testuser")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .email("test.user@example.com")
                .roles(Set.of(role))
                .build();
    }

    @Test
    void existsByUsername_WhenUsernameExists_ReturnsTrue() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act
        boolean exists = userRepository.existsByUsername("testuser");

        // Assert
        assertTrue(exists);
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    void existsByUsername_WhenUsernameDoesNotExist_ReturnsFalse() {
        // Arrange
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        // Act
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Assert
        assertFalse(exists);
        verify(userRepository).existsByUsername("nonexistent");
    }

    @Test
    void findAllByPaginationAndSearch_WhenSearchMatches_ReturnsPageOfUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> userPage = new PageImpl<>(Collections.singletonList(user));
        when(userRepository.findAllByPaginationAndSearch(anyString(), any(Pageable.class)))
                .thenReturn(userPage);

        // Act
        Page<UserEntity> result = userRepository.findAllByPaginationAndSearch("test", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(user, result.getContent().get(0));
        verify(userRepository).findAllByPaginationAndSearch("test", pageable);
    }

    @Test
    void findAllByPaginationAndSearch_WhenNoMatches_ReturnsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> emptyPage = new PageImpl<>(Collections.emptyList());
        when(userRepository.findAllByPaginationAndSearch(anyString(), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act
        Page<UserEntity> result = userRepository.findAllByPaginationAndSearch("nomatch", pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        verify(userRepository).findAllByPaginationAndSearch("nomatch", pageable);
    }

    @Test
    void findByUsername_WhenUserExists_ReturnsUser() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Act
        Optional<UserEntity> result = userRepository.findByUsername("testuser");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        assertEquals("testuser", result.get().getUsername());
        assertFalse(result.get().getRoles().isEmpty());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void findByUsername_WhenUserDoesNotExist_ReturnsEmptyOptional() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<UserEntity> result = userRepository.findByUsername("nonexistent");

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByUsername("nonexistent");
    }
}
