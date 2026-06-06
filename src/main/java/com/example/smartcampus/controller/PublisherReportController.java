package com.example.smartcampus.controller;

import com.example.smartcampus.dto.ApiResponse;
import com.example.smartcampus.dto.CommentReportDetailDTO;
import com.example.smartcampus.dto.ModerationReportDTO;
import com.example.smartcampus.dto.PublisherCommentReportGroupDTO;
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
        public ResponseEntity<ApiResponse<List<PublisherCommentReportGroupDTO>>> getMyReports(
                @RequestParam(required = false) ReportStatus status,
                @AuthenticationPrincipal User user) {

            List<PublisherCommentReportGroupDTO> reports =
                    publisherReportService.getReportsForPublisher(user, status);
            return ResponseEntity.ok(ApiResponse.ok("Reportes obtenidos", reports));
}

    @GetMapping("/reports/summary")
    public ResponseEntity<ApiResponse<PublisherReportSummaryDTO>> getReportsSummary(
            @AuthenticationPrincipal User user) {

        PublisherReportSummaryDTO summary = publisherReportService.getSummary(user);
        return ResponseEntity.ok(ApiResponse.ok("Resumen de reportes", summary));
    }
    
    @GetMapping("/comments/{commentId}/reports")
        public ResponseEntity<ApiResponse<List<CommentReportDetailDTO>>> getCommentReportsDetail(
                @PathVariable Long commentId,
                @AuthenticationPrincipal User user) {
        
            List<CommentReportDetailDTO> detail =
                    publisherReportService.getCommentReportsDetail(commentId, user);
        
            return ResponseEntity.ok(ApiResponse.ok("Reportes del comentario obtenidos", detail));
        }
        
}