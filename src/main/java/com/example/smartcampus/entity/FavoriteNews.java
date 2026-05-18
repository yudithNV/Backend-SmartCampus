package com.example.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "favorite_news",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "news_id"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FavoriteNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "news_id", nullable = false)
    private Long newsId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }
}