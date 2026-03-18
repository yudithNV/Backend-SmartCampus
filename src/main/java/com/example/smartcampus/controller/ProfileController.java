package com.example.smartcampus.controller;

import com.example.smartcampus.dto.ProfileUpdateDTO;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

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
}