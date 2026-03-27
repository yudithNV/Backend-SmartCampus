package com.example.smartcampus.dto;

import com.example.smartcampus.entity.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDTO {
    private Long id;
    private String name;
    private String description;
    private EventType eventType;
    private Integer locationId;
    private OffsetDateTime startDatetime;
    private OffsetDateTime endDatetime;
    private Integer maxCapacity;
    private String posterUrl;
    private Integer careerId;
    private UUID authorId;
    private Boolean isActive;
    private Integer categoryId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
