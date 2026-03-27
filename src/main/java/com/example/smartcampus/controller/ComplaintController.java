package com.example.smartcampus.controller;

import com.example.smartcampus.dto.ApiResponse;
import com.example.smartcampus.dto.ComplaintCreateDTO;
import com.example.smartcampus.dto.ComplaintResponseDTO;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final Set<String> ALLOWED_DOC_TYPES = Set.of(
            "application/pdf"
    );

    /**
     * Crear un nuevo reclamo (JSON simple sin archivo)
     * POST /api/complaints
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ComplaintResponseDTO>> createComplaint(
            @Valid @RequestBody ComplaintCreateDTO dto,
            @AuthenticationPrincipal User student) {

        try {
            ComplaintResponseDTO result = complaintService.createComplaint(dto, student, null);
            return ResponseEntity.ok(ApiResponse.ok("Reclamo creado exitosamente", result));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error al crear el reclamo: " + e.getMessage()));
        }
    }

    /**
     * Crear un nuevo reclamo con archivo de evidencia
     * POST /api/complaints/with-evidence
     */
    @PostMapping(value = "/with-evidence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ComplaintResponseDTO>> createComplaintWithEvidence(
            @Valid @RequestPart("complaint") ComplaintCreateDTO dto,
            @RequestPart(value = "evidence", required = false) MultipartFile evidence,
            @AuthenticationPrincipal User student) {

        try {
            // Validar archivo de evidencia si existe
            if (evidence != null && !evidence.isEmpty()) {
                if (evidence.getSize() > MAX_FILE_SIZE) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("El archivo no debe superar 10 MB"));
                }

                String contentType = evidence.getContentType();
                if (contentType == null ||
                        (!ALLOWED_IMAGE_TYPES.contains(contentType) && !ALLOWED_DOC_TYPES.contains(contentType))) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Solo se permiten imágenes (JPG, PNG, WEBP, GIF) o archivos PDF"));
                }
            }

            ComplaintResponseDTO result = complaintService.createComplaint(dto, student, evidence);
            return ResponseEntity.ok(ApiResponse.ok("Reclamo creado exitosamente", result));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error al crear el reclamo: " + e.getMessage()));
        }
    }

    /**
     * Obtener todos los reclamos del estudiante autenticado
     * GET /api/complaints/my
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ComplaintResponseDTO>>> getMyComplaints(
            @AuthenticationPrincipal User student) {
        List<ComplaintResponseDTO> result = complaintService.getComplaintsByStudent(student);
        return ResponseEntity.ok(ApiResponse.ok("Reclamos obtenidos correctamente", result));
    }
}
