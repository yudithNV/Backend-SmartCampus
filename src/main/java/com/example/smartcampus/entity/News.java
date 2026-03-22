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

    // VARCHAR(255) en BD — suficiente para título
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    // TEXT en BD (después de ejecutar fix_news_columns.sql)
    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "category")
    private NewsCategory category;

    // TEXT en BD (después del fix)
    @Column(name = "cover_url", columnDefinition = "TEXT")
    private String coverUrl;

    // TEXT en BD (después del fix)
    @Column(name = "attachment_url", columnDefinition = "TEXT")
    private String attachmentUrl;

    @Column(name = "career_id")
    private Integer careerId;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "published")
    private Boolean published;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}