package com.example.smartcampus.controller;

import com.example.smartcampus.dto.ApiResponse;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.service.PublisherCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/publisher/comments")
@RequiredArgsConstructor
public class PublisherCommentController {

    private final PublisherCommentService publisherCommentService;

    
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteReportedComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal User user) {

        publisherCommentService.deleteComment(commentId, user);

        return ResponseEntity.ok(
            ApiResponse.ok(
                "Comentario eliminado permanentemente",
                null
            )
        );
    }
}