package com.example.smartcampus.repository;

import com.example.smartcampus.entity.FavoriteNews;
import com.example.smartcampus.entity.News;
import com.example.smartcampus.entity.NewsCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Repository
    public interface FavoriteNewsRepository extends JpaRepository<FavoriteNews, Long> {

        @Query("""
            SELECT f.news.id
            FROM FavoriteNews f
            WHERE f.user.id = :userId
            AND f.news.id IN :newsIds
        """)
        Set<Long> findFavoriteNewsIdsByUserIdAndNewsIdIn(
                @Param("userId") UUID userId,
                @Param("newsIds") List<Long> newsIds
        );
    }
}
 