package com.example.smartcampus.repository;

import com.example.smartcampus.entity.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {

    // Todas las sugerencias del estudiante, más recientes primero
    List<Suggestion> findByStudentIdOrderByCreatedAtDesc(UUID studentId);
}