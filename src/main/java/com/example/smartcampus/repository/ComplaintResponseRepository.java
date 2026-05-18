package com.example.smartcampus.repository;

import com.example.smartcampus.entity.ComplaintResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintResponseRepository extends JpaRepository<ComplaintResponse, Long> {

    // Obtener todas las respuestas de un reclamo
    List<ComplaintResponse> findByComplaintIdOrderByCreatedAtDesc(Long complaintId);

    // Obtener la respuesta de cierre (is_closing = true)
    ComplaintResponse findByComplaintIdAndIsClosingTrue(Long complaintId);
}
