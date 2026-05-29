package com.example.smartcampus.controller;

import com.example.smartcampus.dto.ApiResponse;
import com.example.smartcampus.dto.ModerationActionDTO;
import com.example.smartcampus.dto.ModerationReportDTO;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.service.ModerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/moderation")
@RequiredArgsConstructor
public class ModerationController {

    private final ModerationService moderationService;

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<ModerationReportDTO>>> getPendingReports(
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(
            ApiResponse.ok("Reportes pendientes", moderationService.getPendingReports(user))
        );
    }

    
    @PatchMapping("/reports/{id}")
    public ResponseEntity<ApiResponse<ModerationReportDTO>> processReport(
            @PathVariable Long id,
            @Valid @RequestBody ModerationActionDTO dto,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(
            ApiResponse.ok("Reporte procesado", moderationService.processReport(id, dto, user))
        );
    }
}