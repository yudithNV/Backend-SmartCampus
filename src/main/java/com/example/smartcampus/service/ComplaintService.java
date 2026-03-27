package com.example.smartcampus.service;

import com.example.smartcampus.dto.ComplaintCreateDTO;
import com.example.smartcampus.dto.ComplaintResponseDTO;
import com.example.smartcampus.entity.Complaint;
import com.example.smartcampus.entity.ComplaintStatus;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final SupabaseStorageService supabaseStorageService;
    private final Random random = new Random();

    /**
     * Crea un nuevo reclamo
     */
    
    public ComplaintResponseDTO createComplaint(ComplaintCreateDTO dto, User student, MultipartFile evidence) {
        // Subir evidencia si existe
        String evidenceUrl = null;
        if (evidence != null && !evidence.isEmpty()) {
            try {
                // Usamos Exception (en general) para atrapar IOException e InterruptedException
                evidenceUrl = supabaseStorageService.uploadFile(evidence, "complaints");
            } catch (Exception e) { 
                // Esto atrapará CUALQUIER error de la línea 35 o 38
                throw new RuntimeException("Error al procesar el archivo: " + e.getMessage());
            }
        }
        // Generar tracking number único
        String trackingNumber = generateUniqueTrackingNumber();

        // Crear la entidad Complaint
        Complaint complaint = Complaint.builder()
                .studentId(student.getId())
                .trackingNumber(trackingNumber)
                .title(dto.getTitle())
                .body(dto.getBody())
                .category(dto.getCategory())
                .status(ComplaintStatus.PENDIENTE)
                .evidenceUrl(evidenceUrl)
                .build();

        Complaint saved = complaintRepository.save(complaint);
        return mapToDTO(saved);
    }

    /**
     * Obtiene todos los reclamos de un estudiante
     */
    public List<ComplaintResponseDTO> getComplaintsByStudent(User student) {
        return complaintRepository.findByStudentIdOrderByCreatedAtDesc(student.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Genera un tracking number único en formato: REC-YYYYMMDD-XXXXX
     */
    private String generateUniqueTrackingNumber() {
        String trackingNumber;
        do {
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            int randomNum = 10000 + random.nextInt(90000); // Número entre 10000 y 99999
            trackingNumber = "REC-" + date + "-" + randomNum;
        } while (complaintRepository.existsByTrackingNumber(trackingNumber));

        return trackingNumber;
    }

    /**
     * Mapea una entidad Complaint a ComplaintResponseDTO
     */
    private ComplaintResponseDTO mapToDTO(Complaint complaint) {
        return ComplaintResponseDTO.builder()
                .id(complaint.getId())
                .studentId(complaint.getStudentId())
                .trackingNumber(complaint.getTrackingNumber())
                .title(complaint.getTitle())
                .body(complaint.getBody())
                .category(complaint.getCategory())
                .status(complaint.getStatus())
                .evidenceUrl(complaint.getEvidenceUrl())
                .createdAt(complaint.getCreatedAt())
                .updatedAt(complaint.getUpdatedAt())
                .build();
    }
}
