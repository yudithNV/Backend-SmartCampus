package com.example.smartcampus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentCreateDTO {
    @NotBlank(message = "El comentario no puede estar vacío")
    private String body;
}