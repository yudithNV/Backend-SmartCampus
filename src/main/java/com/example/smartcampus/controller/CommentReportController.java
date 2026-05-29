package com.example.smartcampus.controller;

import com.example.smartcampus.dto.ApiResponse;
import com.example.smartcampus.dto.CommentReportRequestDTO;
import com.example.smartcampus.dto.CommentReportResponseDTO;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.service.CommentReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/news/{newsId}/comments/{commentId}/report")
@RequiredArgsConstructor
public class CommentReportController {

    private final CommentReportService reportService;


    @PostMapping
    public ResponseEntity<ApiResponse<CommentReportResponseDTO>> reportComment(
            @PathVariable Long newsId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentReportRequestDTO dto,
            @AuthenticationPrincipal User user) {

        CommentReportResponseDTO result = reportService.reportComment(newsId, commentId, dto, user);
        String msg = result.isAlreadyReported()
            ? "Ya reportaste este comentario anteriormente"
            : "Comentario reportado exitosamente";

        return ResponseEntity.ok(ApiResponse.ok(msg, result));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> checkReportStatus(
            @PathVariable Long newsId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal User user) {

        boolean reported = reportService.hasReported(commentId, user);
        return ResponseEntity.ok(ApiResponse.ok("Estado del reporte", reported));
    }
}