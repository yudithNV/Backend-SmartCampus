package com.example.smartcampus.dto;

import com.example.smartcampus.entity.ReactionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReactionRequestDTO {
    @NotNull(message = "El tipo de reacción es obligatorio")
    private ReactionType reactionType;
}