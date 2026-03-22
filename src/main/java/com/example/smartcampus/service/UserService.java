package com.example.smartcampus.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.smartcampus.dto.UserCreateDTO;
import com.example.smartcampus.dto.UserCreateResponseDTO;
import com.example.smartcampus.dto.UserListDTO;
import com.example.smartcampus.entity.Role;
import com.example.smartcampus.entity.Status;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CareerService careerService;

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

    public UserCreateResponseDTO createUserWithResponse(UserCreateDTO dto) {
        User user = createUser(dto);

        UserCreateResponseDTO.UserRegisterDTO userInfo = new UserCreateResponseDTO.UserRegisterDTO(
            user.getId().toString(),
            user.getFullName(),
            user.getEmail(),
            user.getRole().name(),
            user.getStatus().name(),
            user.getCreatedAt().toString()
        );

        return new UserCreateResponseDTO(
            "✅ Usuario creado exitosamente",
            userInfo
        );
    }

    public List<UserListDTO> listAllUsers() {
        return userRepository.findAll().stream()
            .map(user -> {
                UserListDTO.CareerInfo careerInfo = getCareerInfo(user);
                return new UserListDTO(
                    user.getId(),
                    user.getFullName(),
                    user.getRole().name(),
                    careerInfo,
                    user.getStatus().name(),
                    user.getCreatedAt().toString()
                );
            })
            .toList();
    }

    private UserListDTO.CareerInfo getCareerInfo(User user) {
        if (user.getRole() != Role.ESTUDIANTE || user.getCareerId() == null) {
            return null;
        }
        return careerService.getCareerById(user.getCareerId())
            .map(career -> new UserListDTO.CareerInfo(
                career.getId(),
                career.getName(),
                career.getCode()
            ))
            .orElse(null);
    }

    private String getCareerName(User user) {
        if (user.getRole() != Role.ESTUDIANTE || user.getCareerId() == null) {
            return "N/A";
        }
        return careerService.getCareerNameById(user.getCareerId())
            .orElse("Carrera no encontrada");
    }
}