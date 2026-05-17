package com.example.smartcampus.service;

import com.example.smartcampus.dto.ProfileUpdateDTO;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final long   MAX_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_TYPES = Set.of(
        "image/jpeg", "image/jpg", "image/png"
    );

    private final UserRepository       userRepository;
    private final SupabaseStorageService supabaseStorage;

    public User getProfile(User user) {
        return userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public User updateProfile(ProfileUpdateDTO dto, User user) {
        User existing = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (dto.getFullName()  != null) existing.setFullName(dto.getFullName());
        if (dto.getPhone()     != null) existing.setPhone(dto.getPhone());
        if (dto.getBio()       != null) existing.setBio(dto.getBio());
        if (dto.getAvatarUrl() != null) existing.setAvatarUrl(dto.getAvatarUrl());

        return userRepository.save(existing);
    }

    // ── NUEVO ────────────────────────────────────────────────────────────────
    public String uploadAvatar(MultipartFile file, User user) {
        // Validar tipo
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Solo se permiten imágenes JPG o PNG");
        }
        // Validar tamaño (5 MB)
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("La imagen no puede superar los 5 MB");
        }

        try {
            User existing = userRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Borrar foto anterior si existe
            if (existing.getAvatarUrl() != null && !existing.getAvatarUrl().isBlank()) {
                supabaseStorage.deleteFile(existing.getAvatarUrl());
            }

            // Subir la nueva — carpeta "avatars"
            String publicUrl = supabaseStorage.uploadFile(file, "avatars");

            existing.setAvatarUrl(publicUrl);
            userRepository.save(existing);

            return publicUrl;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al subir la foto: " + e.getMessage(), e);
        }
    }

    public void removeAvatar(User user) {
        try {
            User existing = userRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (existing.getAvatarUrl() != null && !existing.getAvatarUrl().isBlank()) {
                supabaseStorage.deleteFile(existing.getAvatarUrl());
                existing.setAvatarUrl(null);
                userRepository.save(existing);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al quitar la foto: " + e.getMessage(), e);
        }
    }
}