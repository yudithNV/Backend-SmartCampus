package com.example.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDTO {
    private Long id;
    private Long newsId;
    private UUID userId;
    private String userFullName;
    private String userAvatarUrl;
    private String body;
    private Boolean hidden;
    private Boolean isOwn;       
    private Boolean canHide;     
    private OffsetDateTime createdAt;
}