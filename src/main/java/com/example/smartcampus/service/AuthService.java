package com.example.smartcampus.service;

import com.example.smartcampus.dto.LoginRequestDTO;
import com.example.smartcampus.dto.LoginResponseDTO;
import com.example.smartcampus.entity.Status;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.UserRepository;
import com.example.smartcampus.security.JwtService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AccessLogService accessLogService;

    public LoginResponseDTO login(LoginRequestDTO request, String ipAddress, String userAgent) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Correo o contraseña incorrecta"
                ));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            accessLogService.record(request.getEmail(), ipAddress, userAgent, false);

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Correo o contraseña incorrecta"
            );
        }

        if (user.getStatus() == Status.BLOQUEADO) {
            accessLogService.record(request.getEmail(), ipAddress, userAgent, false);

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Tu cuenta ha sido bloqueada. Contacta al administrador."
            );
        }

        if (user.getStatus() == Status.INACTIVO) {
            accessLogService.record(request.getEmail(), ipAddress, userAgent, false);

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Tu cuenta está inactiva. Contacta al administrador."
            );
        }

        accessLogService.record(request.getEmail(), ipAddress, userAgent, true);

        String token = jwtService.generateToken(user);

        String redirectUrl = switch (user.getRole()) {
            case ESTUDIANTE -> "/estudiante/dashboard";
            case PUBLICADOR -> "/publicador/dashboard";
            case ADMINISTRADOR -> "/admin/dashboard";
        };

        return new LoginResponseDTO(
                token,
                user.getRole().name(),
                redirectUrl,
                user.getMustChangePassword()
        );
    }
}