package com.example.smartcampus.service;

import com.example.smartcampus.dto.UserPreferencesDTO;
import com.example.smartcampus.entity.UserPreferences;
import com.example.smartcampus.repository.UserPreferencesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserPreferencesService {

    private final UserPreferencesRepository preferencesRepository;

    public UserPreferencesDTO getPreferences(UUID userId) {
        return preferencesRepository.findByUserId(userId)
                .map(p -> UserPreferencesDTO.builder()
                        .eventTypes(safe(p.getEventTypes()))
                        .categoryIds(safe(p.getCategoryIds()))
                        .build())
                .orElse(UserPreferencesDTO.builder()
                        .eventTypes(new ArrayList<>())
                        .categoryIds(new ArrayList<>())
                        .build());
    }

    public UserPreferencesDTO savePreferences(UUID userId, UserPreferencesDTO dto) {
        UserPreferences prefs = preferencesRepository.findByUserId(userId)
                .orElse(UserPreferences.builder()
                        .userId(userId)
                        .createdAt(OffsetDateTime.now())
                        .build());

        prefs.setEventTypes(safe(dto.getEventTypes()));
        prefs.setCategoryIds(safe(dto.getCategoryIds()));
        prefs.setUpdatedAt(OffsetDateTime.now());

        UserPreferences saved = preferencesRepository.save(prefs);

        return UserPreferencesDTO.builder()
                .eventTypes(safe(saved.getEventTypes()))
                .categoryIds(safe(saved.getCategoryIds()))
                .build();
    }

    public boolean hasPreferences(UUID userId) {
        return preferencesRepository.findByUserId(userId)
                .map(p -> !p.getEventTypes().isEmpty() || !p.getCategoryIds().isEmpty())
                .orElse(false);
    }

    private <T> List<T> safe(List<T> list) {
        return list != null ? list : new ArrayList<>();
    }
}