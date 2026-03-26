package com.example.smartcampus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SuggestionRequestDTO {

    @NotNull(message = "La categoría es obligatoria")
    private String category;  // Se valida como string y se convierte al enum en el service

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String body;
}