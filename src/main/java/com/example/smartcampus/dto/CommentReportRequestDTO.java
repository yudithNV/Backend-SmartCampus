package com.example.smartcampus.dto;

import com.example.smartcampus.entity.ReportReason;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentReportRequestDTO {

    @NotNull(message = "El motivo del reporte es obligatorio")
    private ReportReason reason;

    private String description;
}