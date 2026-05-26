package com.department.identityservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class BlacklistRedisRepository {
    private final RedisTemplate<String, String> redisTemplate;

    // Check token is in black list
    public boolean isBlacklisted(String token)
    {
        return redisTemplate.hasKey("blacklist:"+token);
    }

    public void removeBlacklistedToken(String token)
    {
        redisTemplate.delete("blacklist:"+token);
    }

    public void addToken(String token, long expirations)
    {
        redisTemplate.opsForValue().set("blacklist:"+token, "blacklisted", expirations, TimeUnit.MILLISECONDS);
    }
}
