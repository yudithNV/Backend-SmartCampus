package com.example.smartcampus.service;

import com.example.smartcampus.entity.*;
import com.example.smartcampus.repository.*;
import exception.ForbiddenException;
import exception.NotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublisherCommentService {

    private final NewsCommentRepository commentRepository;
    private final NewsRepository        newsRepository;
    private final CommentReportRepository reportRepository;

   
    @Transactional
    public void deleteComment(Long commentId, User publisher) {
        if (publisher.getRole() != Role.PUBLICADOR && publisher.getRole() != Role.ADMINISTRADOR) {
            throw new ForbiddenException("Sin permisos para esta acción.");
        }

        NewsComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comentario no encontrado"));

        News news = newsRepository.findById(comment.getNewsId())
                .orElseThrow(() -> new NotFoundException("Noticia no encontrada"));

        if (!news.getAuthorId().equals(publisher.getId())) {
            throw new ForbiddenException(
                "Solo puedes eliminar comentarios de tus propias noticias.");
        }

        List<CommentReport> reports = reportRepository
                .findByCommentIdIn(List.of(commentId));
        reports.forEach(r -> {
            r.setStatus(ReportStatus.ELIMINADO);
            r.setReviewedAt(java.time.OffsetDateTime.now());
        });
        reportRepository.saveAll(reports);

        commentRepository.deleteById(commentId);
    }
}