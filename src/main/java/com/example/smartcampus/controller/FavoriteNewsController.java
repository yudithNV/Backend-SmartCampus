package com.example.smartcampus.controller;

import com.example.smartcampus.dto.FavoriteNewsResponseDTO;
import com.example.smartcampus.dto.NewsResponseDTO;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.service.FavoriteNewsService;
import com.example.smartcampus.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/news/favorites")
@RequiredArgsConstructor
public class FavoriteNewsController {

    private final FavoriteNewsService favoriteNewsService;

    
    @PostMapping("/{newsId}")
    public ResponseEntity<FavoriteNewsResponseDTO> addFavorite(
            @PathVariable Long newsId,
            @AuthenticationPrincipal User user) {

        FavoriteNewsResponseDTO dto = favoriteNewsService.addFavorite(newsId, user);
        return ResponseEntity.ok(dto);
    }

    
    @DeleteMapping("/{newsId}")
    public ResponseEntity<Map<String, String>> removeFavorite(
            @PathVariable Long newsId,
            @AuthenticationPrincipal User user) {

        favoriteNewsService.removeFavorite(newsId, user);
        return ResponseEntity.ok(Map.of("message", "Noticia eliminada de favoritos"));
    }

    
    @GetMapping
    public ResponseEntity<List<FavoriteNewsResponseDTO>> getMyFavorites(
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(favoriteNewsService.getMyFavorites(user));
    }

   
    @GetMapping("/{newsId}/status")
    public ResponseEntity<Map<String, Boolean>> isFavorite(
            @PathVariable Long newsId,
            @AuthenticationPrincipal User user) {

        boolean fav = favoriteNewsService.isFavorite(newsId, user);
        return ResponseEntity.ok(Map.of("isFavorite", fav));
    }
}