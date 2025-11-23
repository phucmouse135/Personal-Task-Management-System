package org.example.cv.services;

import org.example.cv.models.requests.UserRequest;
import org.example.cv.models.responses.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    UserResponse createUser(UserRequest userRequest);

    UserResponse getMyInfo();

    UserResponse getUserById(Long id);

    Page<UserResponse> getAllUsers(String search , Pageable pageable);

    UserResponse updateUser(Long id, UserRequest userRequest);

    void softdeleteUser(Long id);

    UserResponse assignRoleToUser(Long userId, String roleId);

    UserResponse removeRoleFromUser(Long userId, String roleId);

    void restoreUser(Long id);

    UserResponse getUserByUsername(String username);
}
