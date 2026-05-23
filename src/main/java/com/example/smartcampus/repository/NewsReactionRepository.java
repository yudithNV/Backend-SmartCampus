package com.example.smartcampus.repository;

import com.example.smartcampus.entity.NewsReaction;
import com.example.smartcampus.entity.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NewsReactionRepository extends JpaRepository<NewsReaction, Long> {

    Optional<NewsReaction> findByNewsIdAndUserId(Long newsId, UUID userId);

    boolean existsByNewsIdAndUserId(Long newsId, UUID userId);

    void deleteByNewsIdAndUserId(Long newsId, UUID userId);

    List<NewsReaction> findAllByNewsId(Long newsId);

    @Query("""
        SELECT r.reactionType, COUNT(r)
        FROM NewsReaction r
        WHERE r.newsId = :newsId
        GROUP BY r.reactionType
    """)
    List<Object[]> countGroupedByType(@Param("newsId") Long newsId);

    @Query("""
        SELECT r.newsId, r.reactionType, COUNT(r)
        FROM NewsReaction r
        WHERE r.newsId IN :newsIds
        GROUP BY r.newsId, r.reactionType
    """)
    List<Object[]> countGroupedByTypeForNewsIds(@Param("newsIds") List<Long> newsIds);
}