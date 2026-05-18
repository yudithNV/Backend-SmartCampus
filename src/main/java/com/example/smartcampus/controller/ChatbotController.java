package com.example.smartcampus.controller;

import com.example.smartcampus.dto.ApiResponse;
import com.example.smartcampus.dto.ChatbotRequestDTO;
import com.example.smartcampus.dto.ChatbotResponseDTO;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * ChatbotController
 * POST /api/chatbot/ask  → procesa la pregunta del estudiante autenticado
 *
 * La seguridad está delegada a SecurityConfig:
 *   .requestMatchers("/api/chatbot/**").hasRole("ESTUDIANTE")
 */
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    /**
     * Recibe la pregunta, consulta a OpenAI, detecta escalamiento,
     * persiste en chatbot_history y devuelve la respuesta.
     *
     * Body: { "question": "¿Cuándo son las inscripciones?" }
     * Response: { answer, escalated, historyId }
     */
    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<ChatbotResponseDTO>> ask(
            @RequestBody @Valid ChatbotRequestDTO request,
            @AuthenticationPrincipal User student) {

        ChatbotResponseDTO response = chatbotService.ask(request, student);

        String message = response.isEscalated()
                ? "Consulta escalada a soporte humano"
                : "Respuesta generada correctamente";

        return ResponseEntity.ok(ApiResponse.ok(message, response));
    }
}