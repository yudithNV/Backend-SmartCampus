package com.example.smartcampus.repository;

import com.example.smartcampus.entity.NewsComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NewsCommentRepository extends JpaRepository<NewsComment, Long> {

    List<NewsComment> findByNewsIdAndHiddenFalseOrderByCreatedAtDesc(Long newsId);

    List<NewsComment> findByNewsIdOrderByCreatedAtDesc(Long newsId);

    Optional<NewsComment> findByIdAndUserId(Long id, UUID userId);

    long countByNewsIdAndHiddenFalse(Long newsId);
    @Query("""
        SELECT c.newsId, COUNT(c)
        FROM NewsComment c
        WHERE c.newsId IN :newsIds AND c.hidden = false
        GROUP BY c.newsId
    """)
    List<Object[]> countVisibleForNewsIds(@Param("newsIds") List<Long> newsIds);

    @Query("SELECT c.id FROM NewsComment c")
        List<Long> findAllCommentIds();
}