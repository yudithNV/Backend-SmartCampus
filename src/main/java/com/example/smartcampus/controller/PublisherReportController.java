package com.example.smartcampus.controller;

import com.example.smartcampus.dto.ApiResponse;
import com.example.smartcampus.dto.ModerationReportDTO;
import com.example.smartcampus.dto.PublisherReportSummaryDTO;
import com.example.smartcampus.entity.ReportStatus;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.service.PublisherReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publisher")
@RequiredArgsConstructor
public class PublisherReportController {

    private final PublisherReportService publisherReportService;

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<ModerationReportDTO>>> getMyReports(
            @RequestParam(required = false) ReportStatus status,
            @AuthenticationPrincipal User user) {

        List<ModerationReportDTO> reports = publisherReportService.getReportsForPublisher(user, status);
        return ResponseEntity.ok(ApiResponse.ok("Reportes obtenidos", reports));
    }

    @GetMapping("/reports/summary")
    public ResponseEntity<ApiResponse<PublisherReportSummaryDTO>> getReportsSummary(
            @AuthenticationPrincipal User user) {

        PublisherReportSummaryDTO summary = publisherReportService.getSummary(user);
        return ResponseEntity.ok(ApiResponse.ok("Resumen de reportes", summary));
    }
}