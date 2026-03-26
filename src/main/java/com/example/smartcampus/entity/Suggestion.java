package com.example.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import com.example.smartcampus.converter.PostgreSQLEnumType;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "suggestions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Suggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "category", nullable = false)
    @Type(value = PostgreSQLEnumType.class)
    private SuggestionCategory category;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}