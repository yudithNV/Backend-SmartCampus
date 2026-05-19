package com.example.smartcampus.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.smartcampus.entity.News;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    // Noticias publicadas
    List<News> findAllByNewsStatusOrderByCreatedAtDesc(com.example.smartcampus.entity.NewsStatus newsStatus);
    
    // Noticias de un autor
    List<News> findAllByAuthorIdOrderByCreatedAtDesc(UUID authorId);

    @Query("SELECT n FROM News n " +
           "WHERE n.newsStatus = 'PROGRAMADO' " +
           "AND n.scheduledAt <= :now")
    List<News> findDueScheduledNews(@Param("now") OffsetDateTime now);
 
    @Modifying
    @Query("UPDATE News n SET n.newsStatus = 'PUBLICADO', n.published = true, n.updatedAt = :now " +
           "WHERE n.newsStatus = 'PROGRAMADO' AND n.scheduledAt <= :now")
    int publishDueNews(@Param("now") OffsetDateTime now);
 
    // ── Paginación con filtros ─────
    @Query(value = """
        SELECT * FROM news
        WHERE status = 'PUBLICADO'
          AND (:search   IS NULL OR LOWER(title) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:careerId IS NULL OR career_id = :careerId)
          AND (CAST(:category AS TEXT) IS NULL OR category::text = CAST(:category AS TEXT))
        """,
        countQuery = """
        SELECT COUNT(*) FROM news
        WHERE status = 'PUBLICADO'
          AND (:search   IS NULL OR LOWER(title) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:careerId IS NULL OR career_id = :careerId)
          AND (CAST(:category AS TEXT) IS NULL OR category::text = CAST(:category AS TEXT))
        """,
        nativeQuery = true)
    Page<News> findFiltered(
            @Param("search")   String search,
            @Param("careerId") Integer careerId,
            @Param("category") String category,
            Pageable pageable);

    // Contar noticias publicadas (PUBLICADO status)
    @Query("SELECT COUNT(n) FROM News n WHERE n.newsStatus = 'PUBLICADO'")
    long countPublishedNews();

    // Contar noticias agrupadas por categoría (solo PUBLICADO)
    @Query("SELECT CAST(n.category AS string), COUNT(n) FROM News n " +
           "WHERE n.newsStatus = 'PUBLICADO' " +
           "GROUP BY n.category " +
           "ORDER BY COUNT(n) DESC")
    List<Object[]> countNewsByCategory();
}
 