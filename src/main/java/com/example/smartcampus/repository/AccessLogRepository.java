package com.example.smartcampus.repository;

import com.example.smartcampus.entity.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

    // Solo intentos fallidos, más recientes primero
    List<AccessLog> findBySuccessFalseOrderByCreatedAtDesc();

    // Contar cuántas veces falló un correo (para detectar sospechosos)
    long countByEmailAndSuccessFalse(String email);

    // Correos con más de N intentos fallidos — para las alertas visuales
    @Query("""
        SELECT a.email, COUNT(a.id) as total
        FROM AccessLog a
        WHERE a.success = false
        GROUP BY a.email
        HAVING COUNT(a.id) >= :threshold
        ORDER BY total DESC
    """)
    List<Object[]> findSuspiciousEmails(int threshold);
}