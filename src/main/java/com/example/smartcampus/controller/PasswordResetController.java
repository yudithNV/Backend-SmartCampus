package com.example.smartcampus.controller;

import com.example.smartcampus.dto.ApiResponse;
import com.example.smartcampus.dto.ForgotPasswordRequestDTO;
import com.example.smartcampus.dto.ResetPasswordRequestDTO;
import com.example.smartcampus.dto.ValidateTokenResponseDTO;
import com.example.smartcampus.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDTO dto) {

        passwordResetService.requestPasswordReset(dto.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(
            "Si el correo está registrado, recibirás un enlace para restablecer tu contraseña.", null));
    }


    @GetMapping("/validate-reset-token")
    public ResponseEntity<ValidateTokenResponseDTO> validateToken(
            @RequestParam String token) {

        boolean valid = passwordResetService.validateToken(token);
        String message = valid
                ? "Token válido"
                : "El enlace expiró o ya fue utilizado. Solicita uno nuevo.";
        return ResponseEntity.ok(new ValidateTokenResponseDTO(valid, message));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO dto) {

        try {
            passwordResetService.resetPassword(
                    dto.getToken(), dto.getNewPassword(), dto.getConfirmPassword());
            return ResponseEntity.ok(ApiResponse.ok(
                "Contraseña actualizada correctamente. Ya puedes iniciar sesión.", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}