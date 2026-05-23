package com.example.smartcampus.dto;

import com.example.smartcampus.entity.SuggestionCategory;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class AdminSuggestionResponseDTO {
    private Long id;
    private String studentName;   // nombre del estudiante
    private SuggestionCategory category; // enum real, NO String
    private String body;
    private OffsetDateTime createdAt;
}