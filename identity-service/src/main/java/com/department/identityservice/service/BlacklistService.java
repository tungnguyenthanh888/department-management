package com.department.identityservice.service;

import com.department.identityservice.entity.UserEntity;
import com.department.identityservice.repository.BlacklistRedisRepository;

public interface BlacklistService {
    void addToken(String token, long expiration);
    void delete(String token);
    boolean isBlacklisted(UserEntity user);
}
