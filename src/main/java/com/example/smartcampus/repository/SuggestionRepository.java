package com.example.smartcampus.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.smartcampus.entity.Suggestion;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, Long>, JpaSpecificationExecutor<Suggestion> {

    // Todas las sugerencias del estudiante, más recientes primero
    List<Suggestion> findByStudentIdOrderByCreatedAtDesc(UUID studentId);

    // Buscar por id y studentId — para validar que la sugerencia pertenece al estudiante
    Optional<Suggestion> findByIdAndStudentId(Long id, UUID studentId);

    // Contar total de sugerencias
    @Query("SELECT COUNT(s) FROM Suggestion s")
    long countAllSuggestions();

    // Contar sugerencias agrupadas por categoría
    // Retorna [category_name, count]
    @Query(value = "SELECT s.category::text, COUNT(*) FROM suggestions s GROUP BY s.category ORDER BY COUNT(*) DESC", nativeQuery = true)
    List<Object[]> countSuggestionsByCategory();
}