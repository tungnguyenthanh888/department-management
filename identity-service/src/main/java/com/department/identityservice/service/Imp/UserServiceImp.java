package com.department.identityservice.service.Imp;

import com.department.identityservice.dto.request.RegisterDTO;
import com.department.identityservice.dto.response.UserResponseDTO;
import com.department.identityservice.entity.UserEntity;
import com.department.identityservice.repository.UserRepository;
import com.department.identityservice.service.UserService;
import com.department.identityservice.types.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {
    @Autowired
    private final UserRepository repository;

    @Override
    public UserResponseDTO createUser(RegisterDTO payload) {
        UserEntity user = new UserEntity();
        user.setUsername(payload.getUsername());
        user.setPassword(payload.getPassword());
        user.setRole(Role.ROLE_USER);
        repository.save(user);
        return UserResponseDTO.builder()
                .username(user.getUsername())
                .role(user.getRole().toString())
                .build();
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    @Override
    public Boolean isExistedUser(String username) {
        return repository.findByUsername(username).isPresent();
    }
}
