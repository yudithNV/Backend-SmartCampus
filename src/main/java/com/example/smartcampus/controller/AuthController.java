package com.example.smartcampus.controller;

import com.example.smartcampus.dto.ApiResponse;
import com.example.smartcampus.dto.LoginRequestDTO;
import com.example.smartcampus.dto.LoginResponseDTO;
import com.example.smartcampus.dto.UserInfoDTO;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request,
                                   HttpServletRequest httpRequest) {   // ← NUEVO parámetro
        try {
            // Extrae IP real considerando proxies
            String ip = httpRequest.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) ip = httpRequest.getRemoteAddr();

            String userAgent = httpRequest.getHeader("User-Agent");

            return ResponseEntity.ok(authService.login(request, ip, userAgent));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoDTO>> getMyInfo(@AuthenticationPrincipal User user) {
        UserInfoDTO info = UserInfoDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();

        return ResponseEntity.ok(ApiResponse.ok("Información del usuario obtenida", info));
    }

    @GetMapping("/test")
    public String test() {
        return "login endpoint funcionando";
    }
}