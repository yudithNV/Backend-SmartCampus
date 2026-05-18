package com.example.smartcampus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintResponseCreateDTO {

    @NotBlank(message = "El cuerpo de la respuesta no puede estar vacío")
    private String body;
}
