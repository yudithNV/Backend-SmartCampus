package com.example.smartcampus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SuggestionReplyDTO {
    @NotBlank(message = "La respuesta no puede estar vacía")
    private String adminResponse;
}