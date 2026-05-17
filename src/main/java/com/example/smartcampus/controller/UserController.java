package com.example.smartcampus.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.example.smartcampus.dto.ApiResponse;
import com.example.smartcampus.dto.UpdateUserStatusDTO;
import com.example.smartcampus.dto.UserCreateDTO;
import com.example.smartcampus.dto.UserListDTO;
import com.example.smartcampus.dto.UserUpdateDTO;
import com.example.smartcampus.entity.Status;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.service.AccessLogService;
import com.example.smartcampus.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AccessLogService accessLogService;

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUserWithResponse(dto));
    }

    @GetMapping
    public ResponseEntity<Page<UserListDTO>> listAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String career,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortType) {
        return ResponseEntity.ok(
            userService.listAllUsers(search, career, role, status, page, size, sortBy, sortType)
        );
    }

    // ─── PUT /api/users/{id} — solo ADMINISTRADOR ─────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserListDTO>> updateUser(
            @PathVariable UUID id,
            @RequestBody UserUpdateDTO dto) {
        try {
            UserListDTO updated = userService.updateUser(id, dto);
            return ResponseEntity.ok(ApiResponse.ok("Datos actualizados correctamente", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.ok(e.getMessage(), null));
        }
    }
    // ─── /api/users/{id}/status 
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserListDTO>> updateUserStatus(
            @PathVariable UUID id,
            @RequestBody UpdateUserStatusDTO dto,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest httpRequest) {

        try {

            UserListDTO updated = userService.updateUserStatus(
                    id,
                    dto.getStatus(),
                    currentUser.getId());

            String ip = httpRequest.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) ip = httpRequest.getRemoteAddr();
            String action = dto.getStatus() == Status.BLOQUEADO ? "BLOCKED" : "UNBLOCKED";
            accessLogService.recordAdminAction(updated.getEmail(), ip,
                httpRequest.getHeader("User-Agent"), action);

            return ResponseEntity.ok(
                    ApiResponse.ok(
                            "Estado actualizado correctamente",
                            updated));

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ─── DELETE /api/users/{id} — solo ADMINISTRADOR ──────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        try {
            userService.deleteUser(id, currentUser.getId());
            return ResponseEntity.ok(ApiResponse.ok("Usuario eliminado correctamente", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.ok(e.getMessage(), null));
        }
    }
}