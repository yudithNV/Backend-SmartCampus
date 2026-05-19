package com.example.smartcampus.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.smartcampus.entity.Reminder;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    Optional<Reminder> findByEventRegistrationId(Long eventRegistrationId);

    // Busca recordatorios pendientes cuya hora programada ya llegó
    List<Reminder> findBySentFalseAndScheduledAtBefore(OffsetDateTime now);

    void deleteByEventRegistrationId(Long eventRegistrationId);
}