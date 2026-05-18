package com.example.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class FavoriteNewsResponseDTO {
    private Long    favoriteId;
    private UUID    userId;
    private Long    newsId;
    private String  newsTitle;
    private String  newsCategory;
    private String  newsCoverUrl;
    private OffsetDateTime newsCreatedAt;
    private OffsetDateTime favoritedAt;
}