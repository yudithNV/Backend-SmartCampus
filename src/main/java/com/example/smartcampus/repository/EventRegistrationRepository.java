package com.example.smartcampus.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.smartcampus.entity.EventRegistration;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    // Buscar registros por estudiante
    List<EventRegistration> findByStudentId(UUID studentId);

    // Buscar registros por evento
    List<EventRegistration> findByEventId(Long eventId);

    // Contar registros por evento
    long countByEventId(Long eventId);

    // Verificar si un estudiante está registrado en un evento
    boolean existsByEventIdAndStudentId(Long eventId, UUID studentId);

    // Eliminar registro por evento y estudiante
    long deleteByEventIdAndStudentId(Long eventId, UUID studentId);

    // Obtener eventos registrados en un rango de fechas (mes)
    @Query("SELECT er FROM EventRegistration er " +
           "JOIN Event e ON er.eventId = e.id " +
           "WHERE er.studentId = :studentId " +
           "AND e.isActive = true " +
           "AND e.startDatetime >= :startDate " +
           "AND e.startDatetime < :endDate " +
           "ORDER BY e.startDatetime ASC")
    List<EventRegistration> findRegisteredEventsByMonthAndStudent(
            @Param("studentId") UUID studentId,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate
    );

    // Obtener eventos registrados en un rango de fechas + carrera
    @Query("SELECT er FROM EventRegistration er " +
           "JOIN Event e ON er.eventId = e.id " +
           "WHERE er.studentId = :studentId " +
           "AND e.isActive = true " +
           "AND e.startDatetime >= :startDate " +
           "AND e.startDatetime < :endDate " +
           "AND e.careerId = :careerId " +
           "ORDER BY e.startDatetime ASC")
    List<EventRegistration> findRegisteredEventsByMonthStudentAndCareer(
            @Param("studentId") UUID studentId,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            @Param("careerId") Integer careerId
    );

    // Obtener eventos registrados en un rango de fechas + categoría
    @Query("SELECT er FROM EventRegistration er " +
           "JOIN Event e ON er.eventId = e.id " +
           "WHERE er.studentId = :studentId " +
           "AND e.isActive = true " +
           "AND e.startDatetime >= :startDate " +
           "AND e.startDatetime < :endDate " +
           "AND e.categoryId = :categoryId " +
           "ORDER BY e.startDatetime ASC")
    List<EventRegistration> findRegisteredEventsByMonthStudentAndCategory(
            @Param("studentId") UUID studentId,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            @Param("categoryId") Integer categoryId
    );

    // Obtener eventos registrados en un rango de fechas + carrera + categoría
    @Query("SELECT er FROM EventRegistration er " +
           "JOIN Event e ON er.eventId = e.id " +
           "WHERE er.studentId = :studentId " +
           "AND e.isActive = true " +
           "AND e.startDatetime >= :startDate " +
           "AND e.startDatetime < :endDate " +
           "AND e.careerId = :careerId " +
           "AND e.categoryId = :categoryId " +
           "ORDER BY e.startDatetime ASC")
    List<EventRegistration> findRegisteredEventsByMonthStudentCareerAndCategory(
            @Param("studentId") UUID studentId,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            @Param("careerId") Integer careerId,
            @Param("categoryId") Integer categoryId
    );
}
