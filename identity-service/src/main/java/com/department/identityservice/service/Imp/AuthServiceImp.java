package com.department.identityservice.service.Imp;

import com.department.identityservice.dto.request.LoginDTO;
import com.department.identityservice.dto.request.RegisterDTO;
import com.department.identityservice.dto.response.AuthResponseDTO;
import com.department.identityservice.dto.response.UserResponseDTO;
import com.department.identityservice.entity.UserEntity;
import com.department.identityservice.jwt.JwtUtils;
import com.department.identityservice.service.AuthService;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private JwtUtils jwtUtils;

    @Override
    public UserResponseDTO register(RegisterDTO payload) {
        if(userServiceImp.isExistedUser(payload.getUsername()))
        {
            throw new BadRequestException("Username is existed.");
        }

        String passwordHashed = passwordEncoder.encode(payload.getPassword());
        payload.setPassword(passwordHashed);

        return userServiceImp.createUser(payload);
    }

    @Override
    public AuthResponseDTO login(LoginDTO payload) {
        Optional<String> token = userServiceImp.findByUsername(payload.getUsername())
                .filter(user -> passwordEncoder.matches(payload.getPassword(), user.getPassword()))
                .map(jwtUtils::generateToken);

        return token.map(s -> AuthResponseDTO.builder()
                .token(s)
                .build()).orElse(null);
    }
}
