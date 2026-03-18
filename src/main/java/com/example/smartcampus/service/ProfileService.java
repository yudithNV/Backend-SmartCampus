package com.example.smartcampus.service;

import com.example.smartcampus.dto.ProfileUpdateDTO;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    public User getProfile(User user) {
        return userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public User updateProfile(ProfileUpdateDTO dto, User user) {
        User existing = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (dto.getFullName() != null) existing.setFullName(dto.getFullName());
        if (dto.getPhone()    != null) existing.setPhone(dto.getPhone());
        if (dto.getBio()      != null) existing.setBio(dto.getBio());
        if (dto.getAvatarUrl()!= null) existing.setAvatarUrl(dto.getAvatarUrl());

        return userRepository.save(existing);
    }
}