package com.example.smartcampus.controller;

import com.example.smartcampus.dto.NewsCreateDTO;
import com.example.smartcampus.dto.NewsResponseDTO;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.service.NewsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @PostMapping
    public ResponseEntity<NewsResponseDTO> create(
            @Valid @RequestBody NewsCreateDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(newsService.createNews(dto, user));
    }


    // ── Listar TODAS las noticias publicadas (público) ──
    @GetMapping
    public ResponseEntity<List<NewsResponseDTO>> getAll() {
        return ResponseEntity.ok(newsService.getAllPublished());
    }

    // ── Listar las noticias del publicador autenticado ──
    @GetMapping("/my")
    public ResponseEntity<List<NewsResponseDTO>> getMyNews(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(newsService.getNewsByAuthor(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NewsResponseDTO> update(
            @PathVariable Long id,        
            @RequestBody NewsCreateDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(newsService.updateNews(id, dto, user));
    }


    @GetMapping("/{id}")
    public ResponseEntity<NewsResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(newsService.getNewsById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        newsService.deleteNews(id, user);
        return ResponseEntity.ok().build();
    }
}