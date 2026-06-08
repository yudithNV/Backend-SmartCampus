package com.example.smartcampus.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventAttendeeDTO {
    private UUID id;
    private String nombreCompleto;
    private String correo;
    private String carreraArea;
    private OffsetDateTime fechaInscripcion;
    private String estado;
}
