package com.example.smartcampus.controller;

import com.example.smartcampus.dto.AccessLogResponseDTO;
import com.example.smartcampus.dto.ApiResponse;
import com.example.smartcampus.service.AccessLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/access-logs")
@RequiredArgsConstructor
public class AccessLogController {

    private final AccessLogService accessLogService;

    // GET /api/access-logs — solo ADMINISTRADOR (configurado en SecurityConfig)
    @GetMapping
    public ResponseEntity<ApiResponse<List<AccessLogResponseDTO>>> getFailedAttempts() {
        List<AccessLogResponseDTO> result = accessLogService.getFailedAttempts();
        return ResponseEntity.ok(ApiResponse.ok("Historial de accesos obtenido", result));
    }
}