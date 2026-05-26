package com.example.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class SuggestionResponseDTO {
    private Long id;
    private UUID studentId;
    private String category;
    private String body;
    private OffsetDateTime createdAt;
    // Respuesta administrativa visible al estudiante
    private String adminResponse;
    private String respondedByName;
    private OffsetDateTime respondedAt;
}