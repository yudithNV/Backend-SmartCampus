package com.example.smartcampus.controller;

import com.example.smartcampus.dto.ApiResponse;
import com.example.smartcampus.dto.ComplaintDetailDTO;
import com.example.smartcampus.dto.ComplaintResponseCreateDTO;
import com.example.smartcampus.dto.ComplaintResponseDTO;
import com.example.smartcampus.dto.ComplaintResponseDetailDTO;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.service.ComplaintService;
import com.example.smartcampus.service.ComplaintResponseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/complaints")
@RequiredArgsConstructor
public class ComplaintAdminController {

    private final ComplaintService complaintService;
    private final ComplaintResponseService complaintResponseService;

    /**
     * GET /api/admin/complaints
     * Obtener lista de todos los reclamos
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ComplaintResponseDTO>>> getAllComplaints() {
        try {
            List<ComplaintResponseDTO> complaints = complaintService.getAllComplaints();
            return ResponseEntity.ok(
                    ApiResponse.ok("Reclamos obtenidos correctamente", complaints)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error al obtener reclamos: " + e.getMessage()));
        }
    }

    /**
     * GET /api/admin/complaints/{id}
     * Obtener detalle de un reclamo con sus respuestas
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintDetailDTO>> getComplaintDetail(
            @PathVariable Long id) {
        try {
            ComplaintDetailDTO complaint = complaintService.getComplaintDetail(id);
            return ResponseEntity.ok(
                    ApiResponse.ok("Detalle del reclamo obtenido correctamente", complaint)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Reclamo no encontrado"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error al obtener detalle: " + e.getMessage()));
        }
    }

    /**
     * PATCH /api/admin/complaints/{id}/status
     * Marcar un reclamo como EN_REVISION
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ComplaintResponseDTO>> markAsUnderReview(
            @PathVariable Long id) {
        try {
            ComplaintResponseDTO complaint = complaintService.updateStatusToUnderReview(id);
            return ResponseEntity.ok(
                    ApiResponse.ok("Reclamo marcado como EN_REVISION", complaint)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Reclamo no encontrado"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error al actualizar estado: " + e.getMessage()));
        }
    }

    /**
     * POST /api/admin/complaints/{id}/responses
     * Crear una respuesta al reclamo (automáticamente lo marca como RESUELTO)
     */
    @PostMapping("/{id}/responses")
    public ResponseEntity<ApiResponse<ComplaintResponseDetailDTO>> respondComplaint(
            @PathVariable Long id,
            @Valid @RequestBody ComplaintResponseCreateDTO dto,
            @AuthenticationPrincipal User admin) {
        try {
            ComplaintResponseDetailDTO response = complaintResponseService.createResponse(id, dto, admin);
            return ResponseEntity.ok(
                    ApiResponse.ok("Respuesta creada y reclamo marcado como RESUELTO", response)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Reclamo no encontrado"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error al responder: " + e.getMessage()));
        }
    }

    /**
     * GET /api/admin/complaints/{id}/responses
     * Obtener todas las respuestas de un reclamo
     */
    @GetMapping("/{id}/responses")
    public ResponseEntity<ApiResponse<List<ComplaintResponseDetailDTO>>> getComplaintResponses(
            @PathVariable Long id) {
        try {
            List<ComplaintResponseDetailDTO> responses = complaintResponseService.getResponsesByComplaintId(id);
            return ResponseEntity.ok(
                    ApiResponse.ok("Respuestas obtenidas correctamente", responses)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error al obtener respuestas: " + e.getMessage()));
        }
    }
}
