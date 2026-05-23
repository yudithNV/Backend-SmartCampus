package com.example.smartcampus.repository;

import com.example.smartcampus.entity.NewsComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NewsCommentRepository extends JpaRepository<NewsComment, Long> {

    // Todos los comentarios visibles de una noticia, más recientes primero (SCRUM-473)
    List<NewsComment> findByNewsIdAndHiddenFalseOrderByCreatedAtDesc(Long newsId);

    // Para publisher: todos los comentarios incluyendo ocultos
    List<NewsComment> findByNewsIdOrderByCreatedAtDesc(Long newsId);

    // Buscar comentario específico de un usuario (para eliminación/validación)
    Optional<NewsComment> findByIdAndUserId(Long id, UUID userId);

    long countByNewsIdAndHiddenFalse(Long newsId);
}