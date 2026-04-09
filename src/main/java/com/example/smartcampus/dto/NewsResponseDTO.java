package com.example.smartcampus.dto;

import com.example.smartcampus.entity.NewsCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class NewsResponseDTO {
    private Long id;
    private String title;
    private String body;
    private NewsCategory category;
    private String coverUrl;
    private String attachmentUrl;
    private Integer careerId;
    private String careerName;  
    private UUID authorId;
    private String authorName; 
    private Boolean published;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}