package com.example.smartcampus.controller;

import com.example.smartcampus.dto.AdminDashboardDTO;
import com.example.smartcampus.dto.ApiResponse;
import com.example.smartcampus.dto.MonthlyCountDTO;
import com.example.smartcampus.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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

    /**
     * GET /api/dashboard/admin/temporal
     * Obtener series temporales (por mes) para Eventos, Noticias y Usuarios.
     *
     * @param fromYear  Año de inicio (ej. 2024)
     * @param fromMonth Mes de inicio 1-12 (ej. 1)
     * @param toYear    Año de fin   (ej. 2025)
     * @param toMonth   Mes de fin   1-12 (ej. 12)
     */
    @GetMapping("/admin/temporal")
    public ResponseEntity<ApiResponse<Map<String, List<MonthlyCountDTO>>>> getTemporalMetrics(
            @RequestParam int fromYear,
            @RequestParam int fromMonth,
            @RequestParam int toYear,
            @RequestParam int toMonth
    ) {
        try {
            Map<String, List<MonthlyCountDTO>> data =
                    adminDashboardService.getTemporalMetrics(fromYear, fromMonth, toYear, toMonth);
            return ResponseEntity.ok(
                    ApiResponse.ok("Series temporales obtenidas correctamente", data)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error al obtener series temporales: " + e.getMessage()));
        }
    }
}
