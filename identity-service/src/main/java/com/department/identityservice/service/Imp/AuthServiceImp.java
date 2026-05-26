package com.department.identityservice.service.Imp;

import com.department.identityservice.dto.request.LoginDTO;
import com.department.identityservice.dto.request.RegisterDTO;
import com.department.identityservice.dto.request.TokenRefreshRequest;
import com.department.identityservice.dto.response.TokenRefreshResponse;
import com.department.identityservice.dto.response.UserResponseDTO;
import com.department.identityservice.entity.RefreshToken;
import com.department.identityservice.exception.InvalidCredentialException;
import com.department.identityservice.jwt.JwtUtils;
import com.department.identityservice.service.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService
{
    @Autowired
    private UserServiceImp userServiceImp;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenServiceImp tokenServiceImp;

    @Autowired
    private BlacklistServiceImp blacklistServiceImp;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public UserResponseDTO register(RegisterDTO payload) {
        if(userServiceImp.isExistedUser(payload.getUsername()))
        {
            throw new InvalidCredentialException("Username or Password is incorrect.");
        }

        String passwordHashed = passwordEncoder.encode(payload.getPassword());
        payload.setPassword(passwordHashed);

        return userServiceImp.createUser(payload);
    }

    @Override
    public TokenRefreshResponse login(LoginDTO payload) {
        Optional<String> token = userServiceImp.findByUsername(payload.getUsername())
                .filter(user -> passwordEncoder.matches(payload.getPassword(), user.getPassword()))
                .map(jwtUtils::generateAccessToken);

        if(token.isEmpty())
            throw new InvalidCredentialException("Email or password is not incorrect.");

        RefreshToken refreshToken = tokenServiceImp.createRefreshToken(
                userServiceImp.findByUsername(payload.getUsername())
                        .orElseThrow(() -> new NoSuchElementException("Not found user."))
        );

        return token.map(s -> TokenRefreshResponse.builder()
                .accessToken(s)
                .refreshToken(refreshToken.getToken())
                .build()).orElse(null);
    }



    @Override
    public TokenRefreshResponse refresh(TokenRefreshRequest token) {
        // 1. Tìm RefreshToken trong DB bằng chuỗi requestToken.
        String requestRefreshToken = token.getRefreshToken();
        return tokenServiceImp.findRefreshToken(requestRefreshToken)
        // 2. Nếu không tìm thấy hoặc token đã hết hạn (expiryDate < Instant.now()) -> Ném Exception.
                .map(tokenServiceImp::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user ->{
                    // 4. Lấy User từ token cũ, gọi JwtUtils để tạo Access Token mới.
                    // 5. Gọi hàm createRefreshToken (đã viết ở Bài 1) để tạo Refresh Token hoàn toàn mới.
                    String newAccessToken = jwtUtils.generateAccessToken(user);

                    // 6. Trả về cặp Token mới cho Client.
                    return new TokenRefreshResponse(newAccessToken, requestRefreshToken);
                })
                .orElseThrow(() -> new ForbiddenException("\"Refresh Token không hợp lệ hoặc không tồn tại!\""));
    }

    @Override
    public void logout(String token) {
        // 1. Giải mã token để lấy claim "jti" và "exp" (thời gian hết hạn).
        Claims claims = jwtUtils.extractAllClaims(token);
        String jti = claims.get("jti", String.class);
        Date exp = claims.getExpiration();
        String username = claims.getSubject();

        if(jti == null)
        {
            throw new InvalidCredentialException("Token khong hop le. Vui long dang nhap lai.");
        }
        // 2. Tính toán TTL (Time-To-Live): TTL = exp - thời gian hiện tại.
        long ttlMillis = exp.getTime() - System.currentTimeMillis();

        // 3. Lưu key "blacklist:{jti}" vào Redis với giá trị "revoked".

        // 4. Thiết lập thời gian tự động xóa (Expiration) cho key này trong Redis bằng đúng TTL.
        if(ttlMillis > 0){
            blacklistServiceImp.addToken(token,ttlMillis);
        }
        // 5. (Tùy chọn): Xóa luôn Refresh Token tương ứng trong PostgreSQL.
        tokenServiceImp.deleteByUser(username);
    }
}
