package com.example.smartcampus.repository;

import com.example.smartcampus.entity.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {

    // Todas las sugerencias del estudiante, más recientes primero
    List<Suggestion> findByStudentIdOrderByCreatedAtDesc(UUID studentId);

    // Buscar por id y studentId — para validar que la sugerencia pertenece al estudiante
    Optional<Suggestion> findByIdAndStudentId(Long id, UUID studentId);
}