package com.example.smartcampus.dto;

import com.example.smartcampus.entity.NewsCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NewsCreateDTO {

    @NotBlank(message = "El título es obligatorio")
    private String title;

    @NotBlank(message = "El cuerpo de la noticia es obligatorio")
    private String body;

    @NotNull(message = "La categoría es obligatoria")
    private NewsCategory category;

    private String coverUrl;
    private String attachmentUrl;
    private Integer careerId;
    private Boolean published;
}