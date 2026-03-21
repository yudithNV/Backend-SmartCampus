package com.example.smartcampus.repository;

import com.example.smartcampus.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface NewsRepository extends JpaRepository<News, Long> { 
    
    // Todas las noticias publicadas, ordenadas por fecha descendente
    List<News> findAllByPublishedTrueOrderByCreatedAtDesc();
 
    // Todas las noticias de un autor (publicadas + borradores)
    List<News> findAllByAuthorIdOrderByCreatedAtDesc(UUID authorId);
}