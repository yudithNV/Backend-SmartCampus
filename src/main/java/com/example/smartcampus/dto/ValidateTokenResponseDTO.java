package com.example.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidateTokenResponseDTO {
    private boolean valid;
    private String message;
}