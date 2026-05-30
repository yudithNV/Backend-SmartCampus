package com.example.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PublisherReportSummaryDTO {
    private long totalPending;
    private Map<Long, Long> pendingByNewsId;  
}