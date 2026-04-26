package com.example.smartcampus.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.smartcampus.entity.EventRegistration;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    long countByEventId(Long eventId);

    boolean existsByEventIdAndStudentId(Long eventId, UUID studentId);

    long deleteByEventIdAndStudentId(Long eventId, UUID studentId);
}