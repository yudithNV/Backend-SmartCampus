package com.example.smartcampus.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain)
        throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");
    System.out.println(">>> AUTH HEADER: " + authHeader); // <-- log temporal

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
    }

    String token = authHeader.substring(7);

    try {
        String email = jwtService.extractEmail(token);
        System.out.println(">>> EMAIL EXTRAÍDO: " + email); // <-- log temporal

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            System.out.println(">>> ROL DEL USUARIO: " + user.getRole()); // <-- log temporal

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities()
                    );
            SecurityContextHolder.getContext().setAuthentication(auth);
            System.out.println(">>> AUTENTICACIÓN SETEADA OK"); 
        }
    } catch (Exception e) {
        System.out.println(">>> ERROR EN FILTRO JWT: " + e.getMessage());
        System.out.println(">>> Continuando sin autenticación para endpoints públicos...");
        // NO devolvemos 401 automáticamente - dejamos que Spring Security decida
        // basado en la configuración de SecurityConfig si el endpoint requiere auth o no
    }

    filterChain.doFilter(request, response);
}
}