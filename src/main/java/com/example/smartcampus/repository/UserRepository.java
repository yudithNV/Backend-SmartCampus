package com.example.smartcampus.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.smartcampus.entity.User;

// ✅ CAMBIO: Agregamos JpaSpecificationExecutor<User> para usar Specification
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // ── DASHBOARD AGGREGATION QUERIES ──────────────────────────────
    
    /**
     * Cuenta total de usuarios
     * Optimizado: ejecuta COUNT(*) en BD
     */
    @Query("SELECT COUNT(u) FROM User u")
    long countAllUsers();

    /**
     * Cuenta usuarios agrupados por rol
     * Retorna [rol_name, count]
     */
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role ORDER BY COUNT(u) DESC")
    List<Object[]> countUsersByRole();

    /**
     * Cuenta usuarios agrupados por status
     * Retorna [status_name, count]
     */
    @Query("SELECT u.status, COUNT(u) FROM User u GROUP BY u.status ORDER BY COUNT(u) DESC")
    List<Object[]> countUsersByStatus();

    /**
     * Cuenta usuarios agrupados por carrera con nombre de carrera
     * Retorna [careerId, careerName, count]
     * Índice recomendado: CREATE INDEX idx_users_career_id ON users(career_id)
     */
    @Query("""
        SELECT u.careerId, c.name, COUNT(u)
        FROM User u
        LEFT JOIN Career c ON u.careerId = c.id
        WHERE u.careerId IS NOT NULL
        GROUP BY u.careerId, c.name
        ORDER BY COUNT(u) DESC
    """)
    List<Object[]> countUsersByCareer();

    // ── Series temporales (dashboard) ────────────────────────────────────────
    /**
     * Cuenta usuarios registrados por mes entre dos fechas.
     * Retorna [año (int), mes (int), count (Long)]
     */
    @Query(value = """
        SELECT EXTRACT(YEAR FROM created_at),
            EXTRACT(MONTH FROM created_at),
            COUNT(id)
        FROM users
        WHERE created_at >= :from
        AND created_at < :to
        GROUP BY EXTRACT(YEAR FROM created_at), EXTRACT(MONTH FROM created_at)
        ORDER BY EXTRACT(YEAR FROM created_at) ASC, EXTRACT(MONTH FROM created_at) ASC
    """, nativeQuery = true)
    List<Object[]> countUsersByMonth(
            @Param("from") java.time.OffsetDateTime from,
            @Param("to")   java.time.OffsetDateTime to);
}