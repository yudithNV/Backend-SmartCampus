package com.example.smartcampus.service;

import com.example.smartcampus.dto.ChatbotRequestDTO;
import com.example.smartcampus.dto.ChatbotResponseDTO;
import com.example.smartcampus.entity.ChatbotHistory;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.ChatbotHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * ChatbotService — integrado con Groq API (GRATUITO, sin tarjeta, sin restricción de región)
 * ────────────────────────────────────────────────────────────────────────────────────────────
 * Modelo : llama-3.1-8b-instant  → rápido, gratis, ideal para FAQ
 * Key    : console.groq.com → API Keys → Create API Key  (empieza con gsk_...)
 *
 * Groq usa el mismo formato de API que OpenAI, lo que hace la integración muy limpia.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatbotHistoryRepository chatbotHistoryRepository;
    private final RestTemplate restTemplate;

    @Value("${groq.api.key}")
    private String groqApiKey;

    private static final String GROQ_URL   = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_MODEL = "llama-3.1-8b-instant";

    private static final String ESCALATION_SENTINEL = "ESCALAR";
    private static final String ESCALATION_MESSAGE =
            "Lo siento, no tengo información suficiente para responder esa pregunta. " +
            "Te recomiendo revisar el Muro de Noticias en SmartCampus o contactar " +
            "directamente con soporte de la UCB.";

    // ─────────────────────────────────────────────────────────────────────────
    // Método principal
    // ─────────────────────────────────────────────────────────────────────────

    public ChatbotResponseDTO ask(ChatbotRequestDTO request, User student) {
        String question = request.getQuestion().trim();
        String rawAnswer;

        try {
            rawAnswer = callGroq(question);
        } catch (Exception e) {
            log.error("Error llamando a Groq API: {}", e.getMessage(), e);
            rawAnswer = ESCALATION_SENTINEL;
        }

String normalized = rawAnswer == null
        ? ""
        : rawAnswer.toLowerCase();

boolean escalated =
        normalized.contains("escalar")
        || normalized.contains("no tengo información")
        || normalized.contains("no tengo acceso")
        || normalized.contains("no dispongo de información")
        || normalized.contains("no puedo confirmar")
        || normalized.contains("no cuento con información")
        || normalized.contains("consulta soporte")
        || normalized.contains("contactar con soporte");

String cleanedAnswer = rawAnswer == null
        ? ""
        : rawAnswer
            .replace("ESCALAR", "")
            .replace("__ESCALAR__", "")
            .trim();

String finalAnswer = escalated
        ? ESCALATION_MESSAGE
        : cleanedAnswer;

        ChatbotHistory history = ChatbotHistory.builder()
                .user(student)
                .question(question)
                .answer(finalAnswer)
                .escalated(escalated)
                .build();
        ChatbotHistory saved = chatbotHistoryRepository.save(history);

        return ChatbotResponseDTO.builder()
                .answer(finalAnswer)
                .escalated(escalated)
                .historyId(saved.getId())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Llamada HTTP a Groq (formato idéntico a OpenAI)
    // ─────────────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String callGroq(String userQuestion) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        Map<String, Object> systemMessage = Map.of(
                "role", "system",
                "content", buildSystemPrompt()
        );
        Map<String, Object> userMessage = Map.of(
                "role", "user",
                "content", userQuestion
        );

        Map<String, Object> body = Map.of(
                "model", GROQ_MODEL,
                "messages", List.of(systemMessage, userMessage),
                "max_tokens", 400,
                "temperature", 0.3
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(GROQ_URL, entity, Map.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            log.warn("Groq respondió con status: {}", response.getStatusCode());
            return ESCALATION_SENTINEL;
        }

        return extractText(response.getBody());
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> body) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
            if (choices == null || choices.isEmpty()) return ESCALATION_SENTINEL;

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            if (message == null) return ESCALATION_SENTINEL;

            String text = (String) message.get("content");
            return text != null ? text : ESCALATION_SENTINEL;
        } catch (Exception e) {
            log.error("Error parseando respuesta de Groq: {}", e.getMessage(), e);
            return ESCALATION_SENTINEL;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // System prompt — enfocado en SmartCampus UCB
    // ─────────────────────────────────────────────────────────────────────────

    private String buildSystemPrompt() {
        return """
                Eres el asistente virtual de UCB SmartCampus, la plataforma digital de la \
                Universidad Católica Boliviana "San Pablo".

                SmartCampus es una plataforma web donde los estudiantes de la UCB pueden:
                - Ver eventos académicos, culturales y sociales del campus
                - Ver noticias y comunicados oficiales de la universidad
                - Registrarse en eventos de su interés
                - Enviar sugerencias a la universidad
                - Crear y dar seguimiento a reclamos institucionales
                - Ver y editar su perfil universitario
                - Consultar su calendario personal de eventos registrados

                CÓMO FUNCIONA LA PLATAFORMA:

                EVENTOS:
                - Los estudiantes ven todos los eventos en "Muro de Eventos"
                - Pueden filtrar por categoría, fecha o carrera
                - Para registrarse: abren el detalle del evento y hacen click en "Registrarse"
                - Los eventos registrados aparecen en "Mi Calendario"
                - Tipos de eventos: académicos, culturales, deportivos, sociales

                NOTICIAS:
                - Las noticias están en "Muro de Noticias"
                - Son comunicados oficiales publicados por la UCB

                SUGERENCIAS:
                - Sección "Sugerencias" → formulario para enviar sugerencias
                - Categorías: Académico, Infraestructura, Bienestar, Otro
                - Las recibe el equipo administrativo de la UCB

                RECLAMOS:
                - Sección "Reclamos" → botón "Nuevo Reclamo"
                - Cada reclamo tiene número de seguimiento automático (formato RCL-000001)
                - Estados: Pendiente, En Revisión, Resuelto
                - Se puede adjuntar evidencia

                PERFIL:
                - Sección "Mi Perfil" → editar nombre, bio, teléfono, carrera y foto
                - Al primer login el sistema pide cambiar la contraseña por seguridad

                CALENDARIO:
                - Muestra los eventos en los que el estudiante se registró
                - También muestra eventos generales de la UCB

                REGLAS:
                1. Responde SIEMPRE en español, de forma amable y concisa (máximo 2 párrafos).
                2. Responde preguntas sobre SmartCampus: eventos, noticias, sugerencias, \
                   reclamos, perfil y calendario.
                3. Si la pregunta no tiene relación con SmartCampus, la UCB o no tienes suficiente \
                información para responder con seguridad, responde ÚNICAMENTE con: ESCALAR
                4. No inventes información que no esté en este contexto.
                """;
    }
}