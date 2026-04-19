package com.example.smartcampus.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

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

        if (userRepository.existsByEmail(dto.getEmail())) {
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

    // ─── Eliminar usuario por ID ───────────────────────────────────────────────
    public void deleteUser(UUID id, UUID requestingAdminId) {
        // PA: El sistema no permite eliminar al administrador que está en sesión
        if (id.equals(requestingAdminId)) {
            throw new RuntimeException("No puedes eliminar tu propia cuenta de administrador");
        }

        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        userRepository.delete(user);
    }

    // ─── Listar usuarios con paginación, filtros y ordenación ─────────────────
    public Page<UserListDTO> listAllUsers(
            String search,
            String career,
            String role,
            String status,
            int page,
            int size,
            String sortBy,
            String sortType) {

        List<String> allowedSortFields = List.of("createdAt", "fullName", "email", "role", "status");
        String safeSortBy = allowedSortFields.contains(sortBy) ? sortBy : "createdAt";

        Sort sort = sortType != null && sortType.equalsIgnoreCase("ASC")
                ? Sort.by(safeSortBy).ascending()
                : Sort.by(safeSortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<User> spec = UserSpecification.searchByNameOrEmail(search)
            .and(UserSpecification.filterByCareer(career))
            .and(UserSpecification.filterByRole(role))
            .and(UserSpecification.filterByStatus(status));

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
}