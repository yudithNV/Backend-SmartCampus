package com.example.smartcampus.repository;

import com.example.smartcampus.entity.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long>, JpaSpecificationExecutor<AccessLog> {
    // 🔥 NUEVO: TODOS los logs (exitosos y fallidos), ordenados por fecha DESC
    List<AccessLog> findAllByOrderByCreatedAtDesc();
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

    // ── DASHBOARD AGGREGATION QUERIES ──────────────────────────────
    
    /**
     * Cuenta intentos de acceso exitosos
     * Optimizado: ejecuta COUNT(*) con WHERE en BD
     * Índice recomendado: CREATE INDEX idx_access_logs_success ON access_logs(success)
     */
    @Query("SELECT COUNT(a) FROM AccessLog a WHERE a.success = true")
    long countSuccessfulAttempts();

    /**
     * Cuenta intentos de acceso fallidos
     * Optimizado: ejecuta COUNT(*) con WHERE en BD
     */
    @Query("SELECT COUNT(a) FROM AccessLog a WHERE a.success = false")
    long countFailedAttempts();

    /**
     * Obtiene correos sospechosos (más de 3 intentos fallidos)
     * Retorna [email, count]
     * Optimizado: usa GROUP BY y HAVING en BD
     * Índice recomendado: CREATE INDEX idx_access_logs_email_success ON access_logs(email, success)
     */
    @Query("""
        SELECT a.email, COUNT(a.id)
        FROM AccessLog a
        WHERE a.success = false
        GROUP BY a.email
        HAVING COUNT(a.id) > 3
        ORDER BY COUNT(a.id) DESC
    """)
    List<Object[]> findSuspiciousEmailsWithCount();
}