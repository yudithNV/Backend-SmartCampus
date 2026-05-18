package com.example.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintMetricsDTO {

    private Long total;
    private Map<String, Long> byStatus;
    private List<ComplaintByCategoryDTO> byCategory;
}
