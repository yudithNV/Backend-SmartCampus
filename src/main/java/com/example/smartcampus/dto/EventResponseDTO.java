package com.example.smartcampus.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.example.smartcampus.entity.EventType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponseDTO {
    private Long id;
    private String name;
    private String description;
    private EventType eventType;
    private LocationDTO location;
    private OffsetDateTime startDatetime;
    private OffsetDateTime endDatetime;
    private Integer maxCapacity;
    private String posterUrl;
    private CareerDTO career;
    private UUID authorId;
    private String authorName;
    private Boolean isActive;
    private CategoryDTO category;

   
    private Integer categoryId;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Boolean recommended;
    private Long registeredCount;
    private Boolean isRegistered;
    
    @Builder.Default
    private Boolean reminderScheduled = null;
}