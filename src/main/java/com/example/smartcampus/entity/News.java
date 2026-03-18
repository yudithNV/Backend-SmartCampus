package com.example.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "news")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)          // <-- esto resuelve el tipo Postgres nativo
    @Column(name = "category")
    private NewsCategory category;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Column(name = "career_id")
    private Integer careerId;

    @Column(name = "author_id")
    private UUID authorId;

    private Boolean published;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}