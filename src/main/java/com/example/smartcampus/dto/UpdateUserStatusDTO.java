package com.example.smartcampus.dto;

import com.example.smartcampus.entity.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusDTO {

    @NotNull(message = "El estado es obligatorio")
    private Status status;
}