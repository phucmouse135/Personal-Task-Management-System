package org.example.cv.services.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.transaction.Transactional;

import org.example.cv.exceptions.AppException;
import org.example.cv.exceptions.ErrorCode;
import org.example.cv.models.entities.RoleEntity;
import org.example.cv.models.entities.UserEntity;
import org.example.cv.models.requests.UserRequest;
import org.example.cv.models.responses.UserResponse;
import org.example.cv.repositories.RoleRepository;
import org.example.cv.repositories.UserRepository;
import org.example.cv.services.UserService;
import org.example.cv.utils.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        log.info("Creating user: {}", userRequest.getUsername());
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        UserEntity user = userMapper.toEntity(userRequest);
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        HashSet<RoleEntity> roles = new HashSet<>();
        for (Long roleId : userRequest.getRoleIds()) {
            RoleEntity role =
                    roleRepository.findById(roleId).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
            roles.add(role);
        }
        user.setRoles(roles);
        user = userRepository.save(user);
        log.info("Created user: {}", user.getId());
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getUserById(Long id) {
        log.info("Getting user by id: {}", id);
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toResponse(user);
    }

    @Override
    public Page<UserResponse> getAllUsers(int page, int size, String search, String sort, String direction) {
        log.info("Getting all users");
        Pageable pageable = PageRequest.of(page, size).withSort(Sort.by(Sort.Direction.fromString(direction), sort));
        return userRepository.findAllByPaginationAndSearch(search, pageable).map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        log.info("Updating user by id: {}", id);
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        userMapper.updateEntityFromRequest(userRequest, user);
        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }
        Set<RoleEntity> roles = new HashSet<>();
        for (Long roleId : userRequest.getRoleIds()) {
            RoleEntity role =
                    roleRepository.findById(roleId).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
            roles.add(role);
        }
        user.setRoles(roles);
        user = userRepository.save(user);
        log.info("Updated user: {}", user.getId());
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void softdeleteUser(Long id) {
        log.info("Soft deleting user by id: {}", id);
        userRepository.softDeleteByIds(List.of(id));
    }

    @Override
    @Transactional
    public UserResponse assignRoleToUser(Long userId, Long roleId) {
        log.info("Assigning role {} to user {}", roleId, userId);
        UserEntity user =
                userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        RoleEntity role =
                roleRepository.findById(roleId).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        user.getRoles().add(role);
        user = userRepository.save(user);
        log.info("Assigned role {} to user {}", roleId, userId);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse removeRoleFromUser(Long userId, Long roleId) {
        log.info("Removing role {} from user {}", roleId, userId);
        UserEntity user =
                userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        RoleEntity role =
                roleRepository.findById(roleId).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        user.getRoles().remove(role);
        user = userRepository.save(user);
        log.info("Removed role {} from user {}", roleId, userId);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void restoreUser(Long id) {
        log.info("Restoring user by id: {}", id);
        userRepository.restoreById(id);
    }
}
