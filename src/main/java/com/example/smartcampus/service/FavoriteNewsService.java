package com.example.smartcampus.service;

import com.example.smartcampus.dto.FavoriteNewsResponseDTO;
import com.example.smartcampus.entity.FavoriteNews;
import com.example.smartcampus.entity.News;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.FavoriteNewsRepository;
import com.example.smartcampus.repository.NewsRepository;
import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteNewsService {

    private final FavoriteNewsRepository favoriteNewsRepository;
    private final NewsRepository newsRepository;

    
    @Transactional
    public FavoriteNewsResponseDTO addFavorite(Long newsId, User user) {
        // Verificar que la noticia existe
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new NotFoundException("Noticia no encontrada con id: " + newsId));

        // Idempotente: si ya existe, retornar sin insertar duplicado
        if (favoriteNewsRepository.existsByUserIdAndNewsId(user.getId(), newsId)) {
            FavoriteNews existing = favoriteNewsRepository
                    .findByUserIdAndNewsId(user.getId(), newsId)
                    .orElseThrow();
            return toDTO(existing, news);
        }

        FavoriteNews favorite = FavoriteNews.builder()
                .userId(user.getId())
                .newsId(newsId)
                .build();

        return toDTO(favoriteNewsRepository.save(favorite), news);
    }

   
    @Transactional
    public void removeFavorite(Long newsId, User user) {
        // Verificar que existe el marcador
        if (!favoriteNewsRepository.existsByUserIdAndNewsId(user.getId(), newsId)) {
            throw new NotFoundException("Esta noticia no está en tus favoritos");
        }
        favoriteNewsRepository.deleteByUserIdAndNewsId(user.getId(), newsId);
    }

    
    @Transactional(readOnly = true)
    public List<FavoriteNewsResponseDTO> getMyFavorites(User user) {
        List<FavoriteNews> favorites = favoriteNewsRepository
                .findAllByUserIdOrderByCreatedAtDesc(user.getId());

        return favorites.stream()
                .map(fav -> {
                    News news = newsRepository.findById(fav.getNewsId()).orElse(null);
                    if (news == null) return null;
                    return toDTO(fav, news);
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    
    @Transactional(readOnly = true)
    public boolean isFavorite(Long newsId, User user) {
        return favoriteNewsRepository.existsByUserIdAndNewsId(user.getId(), newsId);
    }

    // ── Mapper ────────────────────────────────────────────────────────────
    private FavoriteNewsResponseDTO toDTO(FavoriteNews fav, News news) {
        return new FavoriteNewsResponseDTO(
                fav.getId(),
                fav.getUserId(),
                fav.getNewsId(),
                news.getTitle(),
                news.getCategory() != null ? news.getCategory().name() : null,
                news.getCoverUrl(),
                news.getCreatedAt(),
                fav.getCreatedAt()
        );
    }
}