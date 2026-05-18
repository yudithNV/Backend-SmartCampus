package com.example.smartcampus.repository;

import com.example.smartcampus.entity.FavoriteNews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface FavoriteNewsRepository extends JpaRepository<FavoriteNews, Long> {

    boolean existsByUserIdAndNewsId(UUID userId, Long newsId);

    Optional<FavoriteNews> findByUserIdAndNewsId(UUID userId, Long newsId);

    List<FavoriteNews> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    void deleteByUserIdAndNewsId(UUID userId, Long newsId);

    @Query("SELECT f.newsId FROM FavoriteNews f WHERE f.userId = :userId AND f.newsId IN :newsIds")
    Set<Long> findFavoriteNewsIdsByUserIdAndNewsIdIn(
            @Param("userId") UUID userId,
            @Param("newsIds") List<Long> newsIds
    );
}