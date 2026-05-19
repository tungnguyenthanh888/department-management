package com.department.identityservice.service;

import com.department.identityservice.dto.request.RegisterDTO;
import com.department.identityservice.dto.response.UserResponseDTO;
import com.department.identityservice.entity.UserEntity;

import java.util.Optional;

public interface UserService {
    UserResponseDTO createUser(RegisterDTO payload);
    Optional<UserEntity> findByUsername(String username);
    Boolean isExistedUser(String username);
}
