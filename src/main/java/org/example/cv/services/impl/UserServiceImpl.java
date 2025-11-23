package org.example.cv.services.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.example.cv.exceptions.AppException;
import org.example.cv.exceptions.ErrorCode;
import org.example.cv.models.entities.RoleEntity;
import org.example.cv.models.entities.UserEntity;
import org.example.cv.models.requests.UserRequest;
import org.example.cv.models.responses.UserResponse;
import org.example.cv.repositories.RoleRepository;
import org.example.cv.repositories.UserRepository;
import org.example.cv.services.UserService;
import org.example.cv.utils.AuthenticationUtils; // Giả định có class này
import org.example.cv.utils.mapper.UserMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        UserEntity user = userMapper.toEntity(userRequest);
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        // 2. FIX N+1 Query: Lấy tất cả role trong 1 lần gọi DB
        var roles = new HashSet<>(roleRepository.findAllById(userRequest.getRoleIds()));
        // Kiểm tra xem có role rác không (Optional)
        if (roles.size() != userRequest.getRoleIds().size()) {
            throw new AppException(ErrorCode.ROLE_NOT_EXISTED);
        }
        user.setRoles(roles);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Cacheable(value = "users", key = "#id", cacheManager = "caffeineCacheManager")
    @PostAuthorize("hasRole('ADMIN') or returnObject.username == authentication.name")
    public UserResponse getUserById(Long id) {
        log.info("Getting user by id: {}", id);
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getMyInfo() {
        // Sử dụng Utility class để lấy ID user hiện tại gọn hơn
        Long currentUserId = AuthenticationUtils.getCurrentUserId();
        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toResponse(user);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponse> getAllUsers(String search, Pageable pageable) {
        // Repository cần hỗ trợ findAllByPaginationAndSearch như đã review ở bài trước
        return userRepository.findAllByPaginationAndSearch(search, pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional
    @CachePut(value = "users", key = "#id", cacheManager = "caffeineCacheManager")
    @PreAuthorize("hasRole('ADMIN') or @ownershipSecurity.isOwner(authentication, #id)")
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        log.info("Updating user by id: {}", id);
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateEntityFromRequest(userRequest, user);

        // Update Password nếu có
        if (StringUtils.hasText(userRequest.getPassword())) {
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }

        if (userRequest.getRoleIds() != null && !userRequest.getRoleIds().isEmpty()) {
            var roles = new HashSet<>(roleRepository.findAllById(userRequest.getRoleIds()));
            user.setRoles(roles);
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id", cacheManager = "caffeineCacheManager")
    @PreAuthorize("hasRole('ADMIN') or @ownershipSecurity.isOwner(authentication, #id)")
    public void softdeleteUser(Long id) {
        // Check tồn tại trước cho chắc chắn
        if (!userRepository.existsById(id)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        userRepository.softDeleteByIds(List.of(id));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse assignRoleToUser(Long userId, String roleId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        // Dùng Set nên add trùng cũng không sao, nhưng nếu chưa có mới cần save
        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
            return userMapper.toResponse(userRepository.save(user));
        }
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse removeRoleFromUser(Long userId, String roleId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean removed = user.getRoles().removeIf(r -> r.getName().equals(roleId));

        if (removed) {
            return userMapper.toResponse(userRepository.save(user));
        }
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void restoreUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        userRepository.restoreById(id);
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toResponse(user);
    }
}