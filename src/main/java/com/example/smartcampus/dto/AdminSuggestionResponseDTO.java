package com.example.smartcampus.dto;

import com.example.smartcampus.entity.SuggestionCategory;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class AdminSuggestionResponseDTO {
    private Long id;
    private String studentName;
    private SuggestionCategory category;
    private String body;
    private OffsetDateTime createdAt;
    // Respuesta administrativa
    private String adminResponse;
    private String respondedByName;   // nombre del admin, no el UUID
    private OffsetDateTime respondedAt;
}