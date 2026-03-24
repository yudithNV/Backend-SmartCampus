package com.example.smartcampus.repository;

import com.example.smartcampus.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
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
}
