package com.example.smartcampus.controller;

import com.example.smartcampus.dto.ApiResponse;
import com.example.smartcampus.dto.ReactionRequestDTO;
import com.example.smartcampus.dto.ReactionResponseDTO;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.service.NewsReactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/news/{newsId}/reactions")
@RequiredArgsConstructor
public class NewsReactionController {

    private final NewsReactionService reactionService;

    @GetMapping
    public ResponseEntity<ApiResponse<ReactionResponseDTO>> getReactions(
            @PathVariable Long newsId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                ApiResponse.ok("Reacciones obtenidas", reactionService.getReactions(newsId, user)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReactionResponseDTO>> toggleReaction(
            @PathVariable Long newsId,
            @Valid @RequestBody ReactionRequestDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                ApiResponse.ok("Reacción actualizada",
                        reactionService.toggleReaction(newsId, dto.getReactionType(), user)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<ReactionResponseDTO>> removeReaction(
            @PathVariable Long newsId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                ApiResponse.ok("Reacción eliminada", reactionService.removeReaction(newsId, user)));
    }
}