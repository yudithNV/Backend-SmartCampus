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
import com.example.smartcampus.dto.UserUpdateDTO;
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

    // ─── Actualizar usuario por ID ─────────────────────────────────────────────
    public UserListDTO updateUser(UUID id, UserUpdateDTO dto) {

        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // PA: verificar que el nuevo correo no esté siendo usado por otra persona
        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("El correo ya está en uso por otro usuario");
            }
        }

        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getRole() != null && !dto.getRole().isBlank()) {
            user.setRole(Role.valueOf(dto.getRole()));
        }
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            user.setStatus(Status.valueOf(dto.getStatus()));
        }
        // careerId puede ser null intencionalmente (ej. si cambia a PUBLICADOR)
        if (dto.getCareerId() != null) {
            user.setCareerId(dto.getCareerId());
        } else if ("PUBLICADOR".equals(dto.getRole()) || "ADMINISTRADOR".equals(dto.getRole())) {
            user.setCareerId(null);
        }

        user.setUpdatedAt(OffsetDateTime.now());
        User saved = userRepository.save(user);

        UserListDTO.CareerInfo careerInfo = getCareerInfo(saved);
        return new UserListDTO(
            saved.getId(),
            saved.getFullName(),
            saved.getEmail(),
            saved.getRole().name(),
            careerInfo,
            saved.getStatus().name(),
            saved.getCreatedAt().toString(),
            user.getPhone(),
            user.getBio()

        );
    }

    // ─── Eliminar usuario por ID ───────────────────────────────────────────────
    public void deleteUser(UUID id, UUID requestingAdminId) {
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
                    user.getCreatedAt().toString(),
                    user.getPhone(),
                    user.getBio()

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
    // ─── Cambiar estado (ACTIVO / INACTIVO / BLOQUEADO) 
    public UserListDTO updateUserStatus(UUID id, Status newStatus, UUID requestingAdminId) {

        if (id.equals(requestingAdminId)) {
            throw new RuntimeException("No puedes cambiar el estado de tu propia cuenta");
        }

        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setStatus(newStatus);
        user.setUpdatedAt(OffsetDateTime.now());
        User saved = userRepository.save(user);

        UserListDTO.CareerInfo careerInfo = getCareerInfo(saved);
        return new UserListDTO(
            saved.getId(),
            saved.getFullName(),
            saved.getEmail(),
            saved.getRole().name(),
            careerInfo,
            saved.getStatus().name(),
            saved.getCreatedAt().toString(),
            saved.getPhone(),
            saved.getBio()

        );
    }
    // ─── Obtener perfil público de usuario por ID ─────────────────────────────
    public UserListDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        UserListDTO.CareerInfo careerInfo = getCareerInfo(user);
        return new UserListDTO(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole().name(),
            careerInfo,
            user.getStatus().name(),
            user.getCreatedAt().toString(),
            user.getPhone(),
            user.getBio()

        );
    }
}