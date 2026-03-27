package com.example.smartcampus.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.example.smartcampus.entity.ComplaintStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintResponseDTO {

    private Long id;
    private UUID studentId;
    private String trackingNumber;
    private String title;
    private String body;
    private String category;
    private ComplaintStatus status;
    private String evidenceUrl;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
