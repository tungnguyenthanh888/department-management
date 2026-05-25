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
import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
}
