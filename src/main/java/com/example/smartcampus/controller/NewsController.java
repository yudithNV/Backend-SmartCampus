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
}