package com.example.smartcampus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatbotRequestDTO {

    @NotBlank(message = "La pregunta no puede estar vacía")
    @Size(max = 1000, message = "La pregunta no puede superar los 1000 caracteres")
    private String question;
}