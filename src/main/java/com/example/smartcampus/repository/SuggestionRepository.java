package com.example.smartcampus.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.smartcampus.entity.Suggestion;
import com.example.smartcampus.entity.SuggestionCategory;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {

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

    // Admin: todas las sugerencias con datos del estudiante (sin filtro)
    @Query("SELECT s, u FROM Suggestion s JOIN User u ON s.studentId = u.id ORDER BY s.createdAt DESC")
    List<Object[]> findAllWithStudent();

    // Admin: sugerencias filtradas por categoría
    @Query("SELECT s, u FROM Suggestion s JOIN User u ON s.studentId = u.id WHERE s.category = :category ORDER BY s.createdAt DESC")
    List<Object[]> findAllWithStudentByCategory(@Param("category") SuggestionCategory category);

    // Buscar sugerencia por id con datos del estudiante
    @Query("SELECT s, u FROM Suggestion s JOIN User u ON s.studentId = u.id WHERE s.id = :id")
    Optional<Object[]> findByIdWithStudent(@Param("id") Long id);
}