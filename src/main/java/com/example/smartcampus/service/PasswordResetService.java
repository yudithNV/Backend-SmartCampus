package com.example.smartcampus.service;

import com.example.smartcampus.entity.PasswordResetToken;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.PasswordResetTokenRepository;
import com.example.smartcampus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.reset-token.expiry-minutes:30}")
    private int expiryMinutes;

    private static final Pattern STRONG_PASSWORD = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$"
    );

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email.trim().toLowerCase()).ifPresent(user -> {
            tokenRepository.deleteAllByUserId(user.getId());

            String rawToken = generateSecureToken();

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .userId(user.getId())
                    .token(rawToken)
                    .expiresAt(OffsetDateTime.now().plusMinutes(expiryMinutes))
                    .used(false)
                    .build();
            tokenRepository.save(resetToken);

            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), rawToken);

            log.info("[PasswordResetService] Reset token created for user: {}", email);
        });

        log.info("[PasswordResetService] Reset requested for: {}", email);
    }

    public boolean validateToken(String token) {
        return tokenRepository.findByToken(token)
                .map(PasswordResetToken::isValid)
                .orElse(false);
    }
    @Transactional
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido o expirado"));

        if (!resetToken.isValid()) {
            throw new RuntimeException("El enlace de recuperación ya fue usado o expiró. Solicita uno nuevo.");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Las contraseñas no coinciden");
        }

        if (!STRONG_PASSWORD.matcher(newPassword).matches()) {
            throw new RuntimeException(
                "La contraseña debe tener mínimo 8 caracteres, una mayúscula, " +
                "una minúscula, un número y un símbolo especial"
            );
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("[PasswordResetService] Password reset successfully for user: {}", user.getEmail());
    }

    
    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}