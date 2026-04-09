package com.example.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class AccessLogResponseDTO {
    private Long id;
    private String email;
    private Boolean success;
    private String userAgent;
    private OffsetDateTime createdAt;
    /** true si este correo tiene 3 o más intentos fallidos — para alerta visual */
    private Boolean suspicious;
}