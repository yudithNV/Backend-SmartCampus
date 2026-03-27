package com.example.smartcampus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintCreateDTO {

    @NotBlank(message = "El título es requerido")
    private String title;

    @NotBlank(message = "La categoría es requerida")
    @Pattern(regexp = "Académico|Infraestructura|Trámites|General",
             message = "La categoría debe ser: Académico, Infraestructura, Trámites o General")
    private String category;

    @NotBlank(message = "La descripción es requerida")
    private String body;

    // URL de la evidencia (opcional, se asigna después de subir el archivo)
    private String evidenceUrl;
}
