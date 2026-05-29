package com.example.smartcampus.dto;

import com.example.smartcampus.entity.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ModerationActionDTO {

    @NotNull(message = "La acción es obligatoria")
    private ReportStatus action;  
}