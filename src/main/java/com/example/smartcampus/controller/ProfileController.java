package com.example.smartcampus.controller;

import com.example.smartcampus.dto.ProfileUpdateDTO;
import com.example.smartcampus.dto.UserPreferencesDTO;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.service.ProfileService;
import com.example.smartcampus.service.UserPreferencesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService         profileService;
    private final UserPreferencesService preferencesService;

    @GetMapping
    public ResponseEntity<User> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(profileService.getProfile(user));
    }

    @PutMapping
    public ResponseEntity<User> updateProfile(
            @RequestBody ProfileUpdateDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(profileService.updateProfile(dto, user));
    }

    // ── NUEVO: subir/reemplazar foto de perfil ───────────────────────────────
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) {

        String publicUrl = profileService.uploadAvatar(file, user);
        return ResponseEntity.ok(Map.of("avatarUrl", publicUrl));
    }

    // ── NUEVO: quitar foto de perfil ─────────────────────────────────────────
    @DeleteMapping("/avatar")
    public ResponseEntity<Void> removeAvatar(@AuthenticationPrincipal User user) {
        profileService.removeAvatar(user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/preferences")
    public ResponseEntity<UserPreferencesDTO> getPreferences(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(preferencesService.getPreferences(user.getId()));
    }

    @PutMapping("/preferences")
    public ResponseEntity<UserPreferencesDTO> savePreferences(
            @RequestBody UserPreferencesDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(preferencesService.savePreferences(user.getId(), dto));
    }
}