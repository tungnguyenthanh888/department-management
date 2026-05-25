package com.department.identityservice.service.Imp;

import com.department.identityservice.entity.RefreshToken;
import com.department.identityservice.entity.UserEntity;
import com.department.identityservice.repository.RefreshTokenRepository;
import com.department.identityservice.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImp implements RefreshTokenService {
    @Value("${jwt.expiration.refresh-token}")
    private long refreshTokenExpiration;

    private final RefreshTokenRepository repository;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(UserEntity user) {
        repository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString()) // Sinh mã chuỗi ngẫu nhiên, không cần mã hóa JWT cho đỡ nặng DB
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .build();

        return repository.save(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findRefreshToken(String token)
    {
        return repository.findByToken(token);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken refreshToken) {
        // 3. (QUAN TRỌNG - Rotation): Xóa bản ghi Refresh Token cũ khỏi DB.
        if (refreshToken.getExpiryDate().compareTo(Instant.now()) < 0) {
            repository.delete(refreshToken); // Xoá luôn khỏi DB nếu hết hạn
            throw new ForbiddenException("Refresh token đã hết hạn. Vui lòng đăng nhập lại!");
        }
        return refreshToken;
    }
}
