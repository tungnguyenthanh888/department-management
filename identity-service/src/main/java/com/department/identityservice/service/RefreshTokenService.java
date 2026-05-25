package com.department.identityservice.service;

import com.department.identityservice.entity.RefreshToken;
import com.department.identityservice.entity.UserEntity;

public interface RefreshTokenService {
    public RefreshToken createRefreshToken(UserEntity user);
    public RefreshToken verifyExpiration(String token);
}
