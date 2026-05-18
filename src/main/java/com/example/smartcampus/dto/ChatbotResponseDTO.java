package com.example.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotResponseDTO {

    /** Respuesta generada por el bot (o mensaje de escalamiento) */
    private String answer;

    /**
     * true  → el bot no pudo responder y ofreció escalamiento al estudiante.
     * false → respuesta normal del bot.
     */
    private boolean escalated;

    /** ID del registro guardado en chatbot_history (útil para auditoría) */
    private Long historyId;
}