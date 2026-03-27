package com.example.smartcampus.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.smartcampus.entity.Complaint;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // Buscar todos los reclamos de un estudiante
    List<Complaint> findByStudentIdOrderByCreatedAtDesc(UUID studentId);

    // Verificar si existe un tracking number (para generación única)
    boolean existsByTrackingNumber(String trackingNumber);
}
