package com.example.smartcampus.service;

import com.example.smartcampus.dto.UserCreateDTO;
import com.example.smartcampus.entity.Status;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(UserCreateDTO dto) {

        if(userRepository.existsByEmail(dto.getEmail())){
            throw new RuntimeException("El correo ya existe");
        }

        User user = User.builder()
            .fullName(dto.getFullName())
            .email(dto.getEmail())
            .passwordHash(passwordEncoder.encode(dto.getPassword()))
            .role(dto.getRole())
            .status(Status.ACTIVO)
            .careerId(dto.getCareerId())
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())   
            .build();

        return userRepository.save(user);
    }
}