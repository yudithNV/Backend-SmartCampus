package com.example.smartcampus.controller;

import com.example.smartcampus.dto.ApiResponse;
import com.example.smartcampus.dto.CommentCreateDTO;
import com.example.smartcampus.dto.CommentResponseDTO;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.service.NewsCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news/{newsId}/comments")
@RequiredArgsConstructor
public class NewsCommentController {

    private final NewsCommentService commentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponseDTO>>> getComments(
            @PathVariable Long newsId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                ApiResponse.ok("Comentarios obtenidos", commentService.getComments(newsId, user)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponseDTO>> createComment(
            @PathVariable Long newsId,
            @Valid @RequestBody CommentCreateDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                ApiResponse.ok("Comentario creado", commentService.createComment(newsId, dto, user)));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long newsId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal User user) {
        commentService.deleteComment(newsId, commentId, user);
        return ResponseEntity.ok(ApiResponse.ok("Comentario eliminado", null));
    }

    @PatchMapping("/{commentId}/hide")
    public ResponseEntity<ApiResponse<CommentResponseDTO>> toggleHide(
            @PathVariable Long newsId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                ApiResponse.ok("Visibilidad actualizada",
                        commentService.toggleHideComment(newsId, commentId, user)));
    }
}