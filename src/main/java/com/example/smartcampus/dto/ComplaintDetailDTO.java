package com.example.smartcampus.dto;

import com.example.smartcampus.entity.ComplaintStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintDetailDTO {

    private Long id;
    private UUID studentId;
    private String studentName;
    private String studentEmail;
    private String trackingNumber;
    private String title;
    private String body;
    private String category;
    private ComplaintStatus status;
    private String evidenceUrl;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<ComplaintResponseDetailDTO> responses;
}
