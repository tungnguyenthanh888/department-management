package com.department.identityservice.controller;

import com.department.identityservice.dto.request.LoginDTO;
import com.department.identityservice.dto.request.RegisterDTO;
import com.department.identityservice.dto.request.TokenRefreshRequest;
import com.department.identityservice.dto.response.ApiResponse;
import com.department.identityservice.dto.response.TokenRefreshResponse;
import com.department.identityservice.dto.response.UserResponseDTO;
import com.department.identityservice.service.Imp.AuthServiceImp;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()") // Yêu cầu người dùng đã đăng nhập
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7); // Lấy Access Token từ header "Bearer "

        authServiceImp.logout(token);

        return ResponseEntity.ok("Successfully logged out. Your current session has been invalidated.");
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refreshToken(@RequestBody TokenRefreshRequest request) {
        TokenRefreshResponse response = authServiceImp.refresh(request);

        return ResponseEntity.status(HttpStatus.CREATED.value())
                .body(
                        ApiResponse.success("Access token regenerated"
                                ,HttpStatus.CREATED
                                ,response)
                );
    }

    @GetMapping("/test")
    public String demo()
    {
        return "Hello";
    }
}
