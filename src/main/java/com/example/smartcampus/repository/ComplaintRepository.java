package com.example.smartcampus.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.smartcampus.entity.Complaint;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long>, JpaSpecificationExecutor<Complaint> {

    // Buscar todos los reclamos de un estudiante
    List<Complaint> findByStudentIdOrderByCreatedAtDesc(UUID studentId);

    // Verificar si existe un tracking number (para generación única)
    boolean existsByTrackingNumber(String trackingNumber);

    // Obtener todos los reclamos ordenados por fecha
    List<Complaint> findAllByOrderByCreatedAtDesc();

    // ── DASHBOARD AGGREGATION QUERIES ──────────────────────────────
    
    /**
     * Cuenta total de reclamos
     * Optimizado: ejecuta COUNT(*) en BD
     */
    @Query("SELECT COUNT(c) FROM Complaint c")
    long countAllComplaints();

    /**
     * Cuenta reclamos agrupados por status
     * Retorna [status_name, count]
     * Índice recomendado: CREATE INDEX idx_complaints_status ON complaints(status)
     */
    @Query("SELECT c.status, COUNT(c) FROM Complaint c GROUP BY c.status ORDER BY COUNT(c) DESC")
    List<Object[]> countComplaintsByStatus();

    /**
     * Cuenta reclamos agrupados por categoría
     * Retorna [category_name, count]
     * Índice recomendado: CREATE INDEX idx_complaints_category ON complaints(category)
     */
    @Query("SELECT c.category, COUNT(c) FROM Complaint c GROUP BY c.category ORDER BY COUNT(c) DESC")
    List<Object[]> countComplaintsByCategory();
}
