package com.example.smartcampus.dto;

import com.example.smartcampus.entity.EventType;
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
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
