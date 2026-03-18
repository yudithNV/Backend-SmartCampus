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

    public LoginResponseDTO login(LoginRequestDTO request){

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())){
            throw new RuntimeException("correo o Contraseña incorrecta");
        }
        

        String token = jwtService.generateToken(user);
        String redirectUrl = switch (user.getRole()) {
        case ESTUDIANTE    -> "/estudiante/dashboard";
        case PUBLICADOR    -> "/publicador/dashboard";
        case ADMINISTRADOR -> "/admin/dashboard";
    };

        return new LoginResponseDTO(token, user.getRole().name(), redirectUrl);
    }
}