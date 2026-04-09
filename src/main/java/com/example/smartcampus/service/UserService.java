package com.example.smartcampus.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.smartcampus.dto.UserCreateDTO;
import com.example.smartcampus.dto.UserCreateResponseDTO;
import com.example.smartcampus.dto.UserListDTO;
import com.example.smartcampus.entity.Role;
import com.example.smartcampus.entity.Status;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.UserRepository;
import com.example.smartcampus.specification.UserSpecification;

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
            .mustChangePassword(true)
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

    // ✅ CAMBIO: Ahora acepta paginación, ordenación y filtros
    public Page<UserListDTO> listAllUsers(
            String search, 
            String career,
            String role, 
            String status, 
            int page, 
            int size, 
            String sortBy, 
            String sortType) {
        
        // ✅ Validar que sortBy sea un campo válido para evitar SQL injection
        List<String> allowedSortFields = List.of("createdAt", "fullName", "email", "role", "status");
        String safeSortBy = allowedSortFields.contains(sortBy) ? sortBy : "createdAt";

        // ✅ Construir el Sort (ASC o DESC)
        Sort sort = sortType != null && sortType.equalsIgnoreCase("ASC")
                ? Sort.by(safeSortBy).ascending()
                : Sort.by(safeSortBy).descending();

        // ✅ Crear el Pageable con paginación + ordenación
        Pageable pageable = PageRequest.of(page, size, sort);

        // ✅ Construye la especificación combinando búsqueda + filtros
        Specification<User> spec = UserSpecification.searchByNameOrEmail(search)
            .and(UserSpecification.filterByCareer(career))
            .and(UserSpecification.filterByRole(role))
            .and(UserSpecification.filterByStatus(status));

        // ✅ Busca con Specification + Paginación
        Page<User> users = userRepository.findAll(spec, pageable);

        List<UserListDTO> dtos = users.stream()
            .map(user -> {
                UserListDTO.CareerInfo careerInfo = getCareerInfo(user);
                return new UserListDTO(
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole().name(),
                    careerInfo,
                    user.getStatus().name(),
                    user.getCreatedAt().toString()
                );
            })
            .toList();

        return new PageImpl<>(dtos, pageable, users.getTotalElements());
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