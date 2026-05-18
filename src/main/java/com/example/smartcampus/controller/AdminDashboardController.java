package com.example.smartcampus.controller;

import com.example.smartcampus.dto.AdminDashboardDTO;
import com.example.smartcampus.dto.ApiResponse;
import com.example.smartcampus.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    /**
     * GET /api/dashboard/admin
     * Obtener todas las métricas para el dashboard del administrador
     */
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<AdminDashboardDTO>> getAdminDashboard() {
        try {
            AdminDashboardDTO metrics = adminDashboardService.getDashboardMetrics();
            return ResponseEntity.ok(
                    ApiResponse.ok("Métricas del dashboard obtenidas correctamente", metrics)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error al obtener métricas: " + e.getMessage()));
        }
    }
}
