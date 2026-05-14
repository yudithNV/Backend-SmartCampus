package com.example.smartcampus.service;

import com.example.smartcampus.dto.ComplaintResponseCreateDTO;
import com.example.smartcampus.dto.ComplaintResponseDetailDTO;
import com.example.smartcampus.entity.Complaint;
import com.example.smartcampus.entity.ComplaintResponse;
import com.example.smartcampus.entity.ComplaintStatus;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.ComplaintRepository;
import com.example.smartcampus.repository.ComplaintResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintResponseService {

    private final ComplaintResponseRepository complaintResponseRepository;
    private final ComplaintRepository complaintRepository;

    /**
     * Crea una respuesta a un reclamo y automáticamente lo marca como RESUELTO
     */
    @Transactional
    public ComplaintResponseDetailDTO createResponse(Long complaintId,
                                                     ComplaintResponseCreateDTO dto,
                                                     User admin) {

        // Validar que el reclamo existe
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Reclamo no encontrado"));

        // Crear la respuesta (siempre es de cierre)
        ComplaintResponse response = ComplaintResponse.builder()
                .complaintId(complaintId)
                .adminId(admin.getId())
                .body(dto.getBody())
                .isClosing(true)
                .build();

        ComplaintResponse saved = complaintResponseRepository.save(response);

        // Cambiar estado del reclamo a RESUELTO
        complaint.setStatus(ComplaintStatus.RESUELTO);
        complaintRepository.save(complaint);

        return mapToDTO(saved);
    }

    /**
     * Obtiene todas las respuestas de un reclamo
     */
    public List<ComplaintResponseDetailDTO> getResponsesByComplaintId(Long complaintId) {
        return complaintResponseRepository.findByComplaintIdOrderByCreatedAtDesc(complaintId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Mapea una entidad ComplaintResponse a ComplaintResponseDetailDTO
     */
    private ComplaintResponseDetailDTO mapToDTO(ComplaintResponse response) {
        return ComplaintResponseDetailDTO.builder()
                .id(response.getId())
                .complaintId(response.getComplaintId())
                .adminId(response.getAdminId())
                .body(response.getBody())
                .isClosing(response.getIsClosing())
                .createdAt(response.getCreatedAt())
                .build();
    }
}
