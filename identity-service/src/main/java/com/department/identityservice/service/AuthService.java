package com.department.identityservice.service;

import com.department.identityservice.dto.request.LoginDTO;
import com.department.identityservice.dto.request.RegisterDTO;
import com.department.identityservice.dto.response.AuthResponseDTO;
import com.department.identityservice.dto.response.UserResponseDTO;

public interface AuthService {
    UserResponseDTO register(RegisterDTO payload);
    AuthResponseDTO login(LoginDTO payload);
}
