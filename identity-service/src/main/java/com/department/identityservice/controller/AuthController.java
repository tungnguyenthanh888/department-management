package com.department.identityservice.controller;

import com.department.identityservice.dto.request.LoginDTO;
import com.department.identityservice.dto.request.RegisterDTO;
import com.department.identityservice.dto.request.TokenRefreshRequest;
import com.department.identityservice.dto.response.ApiResponse;
import com.department.identityservice.dto.response.AuthResponseDTO;
import com.department.identityservice.dto.response.TokenRefreshResponse;
import com.department.identityservice.dto.response.UserResponseDTO;
import com.department.identityservice.service.Imp.AuthServiceImp;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/identity")
public class AuthController {
    @Autowired
    private AuthServiceImp authServiceImp;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser(@RequestBody RegisterDTO payload)
    {
        UserResponseDTO response = authServiceImp.register(payload);
        return ResponseEntity
                .status(HttpStatus.CREATED.value())
                .body(
                        ApiResponse.success("User was created.", HttpStatus.CREATED, response)
                );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO payload)
    {
        TokenRefreshResponse response = authServiceImp.login(payload);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED.value())
                .body(
                        ApiResponse.success("Login successfully.", HttpStatus.ACCEPTED, response)
                );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refreshToken(@RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration) // Kiểm tra xem token hết hạn chưa
                .map(RefreshToken::getUser)                 // Lấy UserEntity liên kết ra
                .map(user -> {
                    // Tạo một Access Token mới (15 phút)
                    String newAccessToken = jwtUtils.generateAccessToken(user.getUsername(), user.getRole());

                    // Tạo tiếp một Refresh Token mới (Roll-over xoay vòng để kéo dài thêm 7 ngày từ lúc này)
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

                    TokenRefreshResponse responseData = new TokenRefreshResponse(newAccessToken, newRefreshToken.getToken());

                    return new ResponseEntity<>(
                            ApiResponse.success("Gia hạn token thành công", HttpStatus.OK, responseData),
                            HttpStatus.OK
                    );
                })
                .orElseThrow(() -> new CustomForbiddenException("Refresh Token không hợp lệ hoặc không tồn tại!"));
    }

    @GetMapping("/test")
    public String demo()
    {
        return "Hello";
    }
}
