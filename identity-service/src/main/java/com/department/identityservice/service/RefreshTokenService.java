package com.department.identityservice.service;

import com.department.identityservice.entity.RefreshToken;
import com.department.identityservice.entity.UserEntity;

import java.util.Optional;

public interface RefreshTokenService {
    public RefreshToken createRefreshToken(UserEntity user);
    public Optional<RefreshToken> findRefreshToken(String token);
    public RefreshToken verifyExpiration(RefreshToken refreshToken);
    void deleteByUser(String username);
}
