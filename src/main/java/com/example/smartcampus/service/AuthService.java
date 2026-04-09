package com.example.smartcampus.service;

import com.example.smartcampus.dto.LoginRequestDTO;
import com.example.smartcampus.dto.LoginResponseDTO;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.UserRepository;
import com.example.smartcampus.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AccessLogService accessLogService;   // ← NUEVO

    public LoginResponseDTO login(LoginRequestDTO request, String ipAddress, String userAgent) {  // ← firma actualizada

        // ── Intento fallido: correo no existe ─────────────────────────────────
        User user;
        try {
            user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("correo o Contraseña incorrecta"));
        } catch (RuntimeException e) {
            accessLogService.record(request.getEmail(), ipAddress, userAgent, false);
            throw e;
        }

        // ── Intento fallido: contraseña incorrecta ────────────────────────────
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            accessLogService.record(request.getEmail(), ipAddress, userAgent, false);
            throw new RuntimeException("correo o Contraseña incorrecta");
        }

        // ── Login exitoso ─────────────────────────────────────────────────────
        accessLogService.record(request.getEmail(), ipAddress, userAgent, true);

        String token = jwtService.generateToken(user);
        String redirectUrl = switch (user.getRole()) {
            case ESTUDIANTE    -> "/estudiante/dashboard";
            case PUBLICADOR    -> "/publicador/dashboard";
            case ADMINISTRADOR -> "/admin/dashboard";
        };

        return new LoginResponseDTO(token, user.getRole().name(), redirectUrl, user.getMustChangePassword());
    }
}