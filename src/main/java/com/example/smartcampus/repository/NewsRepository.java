package com.example.smartcampus.repository;

import com.example.smartcampus.entity.News;
import com.example.smartcampus.entity.NewsCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NewsRepository extends JpaRepository<News, Long> {

    // Noticias publicadas
    List<News> findAllByPublishedTrueOrderByCreatedAtDesc();

    // Noticias de un autor
    List<News> findAllByAuthorIdOrderByCreatedAtDesc(UUID authorId);

    // Paginación
    @Query(value = """
        SELECT * FROM news
        WHERE published = true
          AND (:search   IS NULL OR LOWER(title) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:careerId IS NULL OR career_id = :careerId)
          AND (CAST(:category AS TEXT) IS NULL OR category::text = CAST(:category AS TEXT))
        """,
        countQuery = """
        SELECT COUNT(*) FROM news
        WHERE published = true
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
}