package com.department.apigateway.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String SECRET;

    private SecretKey getSignKey()
    {
        byte[] keyBytes = Decoders.BASE64URL.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Giải mã và lấy toàn bộ Payload Claims
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Kiểm tra token có bị chỉnh sửa hoặc hết hạn không
    public void validateToken(String token) {
        Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token);
    }
}
