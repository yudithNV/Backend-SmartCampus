package com.example.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessLogMetricsDTO {

    private Long successfulAttempts;
    private Long failedAttempts;
    private List<SuspiciousEmailDTO> suspiciousEmails;
}
