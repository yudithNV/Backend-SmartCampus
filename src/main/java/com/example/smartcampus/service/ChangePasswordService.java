package com.example.smartcampus.service;

import com.example.smartcampus.dto.ChangePasswordRequestDTO;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ChangePasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── Reglas de seguridad de la contraseña ────────────────────────────────
    // Mínimo 8 caracteres, al menos una mayúscula, una minúscula,
    // un número y un símbolo especial
    private static final Pattern STRONG_PASSWORD = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$"
    );

    public void changePassword(ChangePasswordRequestDTO dto, User user) {

        // 1. Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        // 2. Verificar que la nueva contraseña y la confirmación coincidan
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("La nueva contraseña y la confirmación no coinciden");
        }

        // 3. Verificar que la nueva contraseña no sea igual a la actual
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPasswordHash())) {
            throw new RuntimeException("La nueva contraseña no puede ser igual a la actual");
        }

        // 4. Validar que la nueva contraseña cumpla los requisitos de seguridad
        if (!STRONG_PASSWORD.matcher(dto.getNewPassword()).matches()) {
            throw new RuntimeException(
                "La contraseña debe tener al menos 8 caracteres, " +
                "una mayúscula, una minúscula, un número y un símbolo especial"
            );
        }

        // 5. Guardar nueva contraseña encriptada y marcar must_change_password = false
        User existing = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        existing.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        existing.setMustChangePassword(false);

        userRepository.save(existing);
    }
}