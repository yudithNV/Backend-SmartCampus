package com.example.smartcampus.repository;

import com.example.smartcampus.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Buscar eventos futuros (desde ahora en adelante)
    List<Event> findByEventDateAfterOrderByEventDateAsc(OffsetDateTime currentDate);

    // Buscar eventos pasados
    List<Event> findByEventDateBeforeOrderByEventDateDesc(OffsetDateTime currentDate);

    // Buscar todos los eventos ordenados por fecha
    List<Event> findAllByOrderByEventDateAsc();

    // Buscar eventos por carrera
    List<Event> findByCareerIdOrderByEventDateAsc(Integer careerId);
}
