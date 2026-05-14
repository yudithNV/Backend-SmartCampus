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
public class ComplaintResponseDetailDTO {

    private Long id;
    private Long complaintId;
    private UUID adminId;
    private String adminName;
    private String body;
    private Boolean isClosing;
    private OffsetDateTime createdAt;
}
