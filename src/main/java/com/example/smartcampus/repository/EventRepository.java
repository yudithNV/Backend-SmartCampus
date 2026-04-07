package com.example.smartcampus.repository;

import com.example.smartcampus.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Buscar eventos futuros (desde ahora en adelante)
    List<Event> findByStartDatetimeAfterOrderByStartDatetimeAsc(OffsetDateTime currentDate);

    // Buscar eventos pasados
    List<Event> findByStartDatetimeBeforeOrderByStartDatetimeDesc(OffsetDateTime currentDate);

    // Buscar todos los eventos ordenados por fecha
    List<Event> findAllByOrderByStartDatetimeAsc();

    // Buscar eventos por carrera
    List<Event> findByCareerIdOrderByStartDatetimeAsc(Integer careerId);

    // Buscar eventos por organizador (autor)
    List<Event> findByAuthorIdOrderByCreatedAtDesc(UUID authorId);

    // Verificar conflicto de horario en la misma ubicación
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Event e " +
           "WHERE e.locationId = :locationId " +
           "AND e.id != :eventId " +
           "AND ((e.startDatetime <= :endDatetime AND e.endDatetime >= :startDatetime))")
    boolean existsConflictingEvent(
            @Param("locationId") Integer locationId,
            @Param("startDatetime") OffsetDateTime startDatetime,
            @Param("endDatetime") OffsetDateTime endDatetime,
            @Param("eventId") Long eventId
    );

    // Obtener el evento que genera conflicto (para mensaje detallado)
    @Query("SELECT e FROM Event e " +
           "WHERE e.locationId = :locationId " +
           "AND e.id != :eventId " +
           "AND ((e.startDatetime <= :endDatetime AND e.endDatetime >= :startDatetime)) " +
           "LIMIT 1")
    Optional<Event> findConflictingEvent(
            @Param("locationId") Integer locationId,
            @Param("startDatetime") OffsetDateTime startDatetime,
            @Param("endDatetime") OffsetDateTime endDatetime,
            @Param("eventId") Long eventId
    );

    // Métodos para paginación y búsqueda con filtrado dinámico
    Page<Event> findAllByIsActiveTrue(Pageable pageable);

    // Solo búsqueda por nombre
    Page<Event> findByIsActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);

    // Solo filtro por categoría
    Page<Event> findByIsActiveTrueAndCategoryId(Integer categoryId, Pageable pageable);

    // Solo filtro por carrera
    Page<Event> findByIsActiveTrueAndCareerId(Integer careerId, Pageable pageable);

    // Búsqueda + categoría
    Page<Event> findByIsActiveTrueAndNameContainingIgnoreCaseAndCategoryId(
            String name, Integer categoryId, Pageable pageable);

    // Búsqueda + carrera
    Page<Event> findByIsActiveTrueAndNameContainingIgnoreCaseAndCareerId(
            String name, Integer careerId, Pageable pageable);

    // Categoría + carrera
    Page<Event> findByIsActiveTrueAndCategoryIdAndCareerId(
            Integer categoryId, Integer careerId, Pageable pageable);

    // Búsqueda + categoría + carrera
    Page<Event> findByIsActiveTrueAndNameContainingIgnoreCaseAndCategoryIdAndCareerId(
            String name, Integer categoryId, Integer careerId, Pageable pageable);
}
