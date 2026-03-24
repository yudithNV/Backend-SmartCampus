package com.example.smartcampus.dto;

import com.example.smartcampus.entity.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCreateDTO {
    @NotBlank(message = "El nombre es requerido")
    private String name;

    @NotBlank(message = "La descripción es requerida")
    private String description;

    @NotNull(message = "El tipo de evento es requerido")
    private EventType eventType;

    private String location;

    // Fecha y hora de inicio (separadas)
    @NotBlank(message = "La fecha de inicio es requerida (formato: YYYY-MM-DD)")
    private String startDate;  // ej: "2026-04-15"

    @NotBlank(message = "La hora de inicio es requerida (formato: HH:mm)")
    private String startTime;  // ej: "14:30"

    // Fecha y hora de fin (opcionales)
    private String endDate;    // ej: "2026-04-15"
    private String endTime;    // ej: "18:00"

    private Integer maxCapacity;
    private String posterUrl;
    private Integer careerId;
    private Integer categoryId;

    // true = publicado, false = borrador
    private Boolean publish = true;
}
