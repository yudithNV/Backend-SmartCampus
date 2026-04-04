package com.example.smartcampus.repository;

import com.example.smartcampus.entity.News;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface NewsRepository extends JpaRepository<News, Long> { 
    
    // Todas las noticias publicadas, ordenadas por fecha descendente
    List<News> findAllByPublishedTrueOrderByCreatedAtDesc();
 
    // Todas las noticias de un autor (publicadas + borradores)
    List<News> findAllByAuthorIdOrderByCreatedAtDesc(UUID authorId);

        // nuevos metodos para paginación y búsqueda
    Page<News> findAllByPublishedTrue(Pageable pageable);

    Page<News> findByPublishedTrueAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
        String title, Pageable pageable);
}