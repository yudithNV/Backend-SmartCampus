package com.example.smartcampus.dto;

import com.example.smartcampus.entity.EventCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventCreateDTO {

    @NotBlank(message = "El título es obligatorio")
    private String title;

    private String description;

    @NotNull(message = "La fecha del evento es obligatoria")
    private String eventDate; // ISO 8601 format: "2026-03-25T14:30:00-04:00"

    private String location;

    private String imageUrl;

    @NotNull(message = "La categoría es obligatoria")
    private EventCategory category;

    private Integer careerId; // Opcional: null = evento general, con valor = evento específico de carrera
}
