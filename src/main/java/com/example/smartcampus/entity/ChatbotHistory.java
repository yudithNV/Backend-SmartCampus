package com.example.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * Mapea la tabla "chatbot_history" que ya existe en Supabase.
 *
 * Columnas:
 *   id          bigint PK
 *   user_id     uuid FK → users.id
 *   question    text
 *   answer      text
 *   escalated   boolean (default false)
 *   created_at  timestamptz
 */
@Entity
@Table(name = "chatbot_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relación ManyToOne con User.
     * Usamos LAZY para no cargar el usuario completo en cada consulta.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "text")
    private String question;

    @Column(nullable = false, columnDefinition = "text")
    private String answer;

    /**
     * true  → conversación escalada a soporte humano.
     * false → respondida normalmente por el bot.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean escalated = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}