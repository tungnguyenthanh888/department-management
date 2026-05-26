package com.department.identityservice.service.Imp;

import com.department.identityservice.repository.BlacklistRedisRepository;
import com.department.identityservice.service.BlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlacklistServiceImp implements BlacklistService {
    private final BlacklistRedisRepository repository;

    @Override
    public void addToken(String token, long expiration) {
        repository.addToken(token, expiration);
    }

    @Override
    public void delete(String token) {
        repository.removeBlacklistedToken(token);
    }

    @Override
    public boolean isBlacklisted(String token) {
        return repository.isBlacklisted(token);
    }
}
