package com.department.identityservice.service;

import com.department.identityservice.dto.request.LoginDTO;
import com.department.identityservice.dto.request.RegisterDTO;
import com.department.identityservice.dto.request.TokenRefreshRequest;
import com.department.identityservice.dto.response.TokenRefreshResponse;
import com.department.identityservice.dto.response.UserResponseDTO;

public interface AuthService {
    UserResponseDTO register(RegisterDTO payload);
    TokenRefreshResponse login(LoginDTO payload);
    TokenRefreshResponse refresh(TokenRefreshRequest token);
    void logout(String token);
}
