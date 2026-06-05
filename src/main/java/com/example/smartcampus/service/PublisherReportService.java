package com.example.smartcampus.service;

import com.example.smartcampus.dto.ModerationReportDTO;
import com.example.smartcampus.dto.PublisherReportSummaryDTO;
import com.example.smartcampus.entity.*;
import com.example.smartcampus.repository.*;
import exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublisherReportService {

    private final CommentReportRepository reportRepository;
    private final NewsCommentRepository commentRepository;
    private final NewsRepository newsRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ModerationReportDTO> getReportsForPublisher(User publisher, ReportStatus status) {
        validatePublisher(publisher);

        // Todos los IDs de comentarios que tienen al menos un reporte
        List<Long> allCommentIds = commentRepository.findAllCommentIds();

        if (allCommentIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<CommentReport> reports;

        if (status != null) {
            reports = reportRepository.findByCommentIdInAndStatus(
                    allCommentIds,
                    status
            );
        } else {
            reports = reportRepository.findByCommentIdIn(allCommentIds);
        }

        if (reports.isEmpty()) {
            return Collections.emptyList();
        }

        // Obtener comentarios reportados
        List<Long> reportedCommentIds = reports.stream()
                .map(CommentReport::getCommentId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, NewsComment> commentsMap = commentRepository
                .findAllById(reportedCommentIds)
                .stream()
                .collect(Collectors.toMap(
                        NewsComment::getId,
                        c -> c
                ));

        // Obtener noticias relacionadas
        Set<Long> newsIds = commentsMap.values()
                .stream()
                .map(NewsComment::getNewsId)
                .collect(Collectors.toSet());

        Map<Long, News> newsMap = newsRepository
                .findAllById(new ArrayList<>(newsIds))
                .stream()
                .collect(Collectors.toMap(
                        News::getId,
                        n -> n
                ));

        // Obtener nombres de usuarios reportantes
        Map<UUID, String> userNames = loadUserNames(
                reports.stream()
                        .map(CommentReport::getReporterId)
                        .collect(Collectors.toSet())
        );

        // Total de reportes por comentario
        Map<Long, Long> totalByComment = reportRepository
                .countGroupedByCommentId(reportedCommentIds);

        return reports.stream().map(r -> {
                NewsComment comment = commentsMap.get(r.getCommentId());

                Long newsId = comment != null
                        ? comment.getNewsId()
                        : null;

                // Recuperar el comentario desde BD si no está en el mapa
                if (newsId == null) {
                        newsId = commentRepository.findById(r.getCommentId())
                                .map(NewsComment::getNewsId)
                                .orElse(null);
                }

                News news = newsId != null
                        ? newsMap.get(newsId)
                        : null;

                return ModerationReportDTO.builder()
                        .id(r.getId())
                        .commentId(r.getCommentId())
                        .commentBody(comment != null ? comment.getBody() : "")
                        .newsId(newsId)
                        .newsTitle(news != null ? news.getTitle() : "")
                        .reporterId(r.getReporterId())
                        .reporterName(
                                userNames.getOrDefault(
                                        r.getReporterId(),
                                        "Usuario"
                                )
                        )
                        .reason(r.getReason())
                        .description(r.getDescription())
                        .status(r.getStatus())
                        .createdAt(r.getCreatedAt())
                        .totalReportsForComment(
                                totalByComment.getOrDefault(
                                        r.getCommentId(),
                                        1L
                                )
                        )
                        .build();
                }).collect(Collectors.toList());
    }

   @Transactional(readOnly = true)
        public PublisherReportSummaryDTO getSummary(User publisher) {
        validatePublisher(publisher);

        List<Long> allCommentIds = commentRepository.findAllCommentIds();
        if (allCommentIds.isEmpty()) {
                return PublisherReportSummaryDTO.builder()
                        .totalPending(0).pendingByNewsId(Collections.emptyMap()).build();
        }

        long total = reportRepository.countByCommentIdInAndStatus(allCommentIds, ReportStatus.PENDIENTE);

        Map<Long, Long> pendingByNews = new HashMap<>();
        List<CommentReport> pending = reportRepository
                .findByCommentIdInAndStatus(allCommentIds, ReportStatus.PENDIENTE);

        Map<Long, Long> commentToNews = commentRepository.findAllById(allCommentIds)
                .stream().collect(Collectors.toMap(NewsComment::getId, NewsComment::getNewsId));

        pending.forEach(r -> {
                Long newsId = commentToNews.get(r.getCommentId());
                if (newsId != null) pendingByNews.merge(newsId, 1L, Long::sum);
        });

        return PublisherReportSummaryDTO.builder()
                .totalPending(total)
                .pendingByNewsId(pendingByNews)
                .build();
        }

    private void validatePublisher(User user) {
        if (user.getRole() != Role.PUBLICADOR && user.getRole() != Role.ADMINISTRADOR) {
                throw new ForbiddenException("Solo los publicadores pueden acceder a esta sección.");
        }
        }

    private Map<UUID, String> loadUserNames(Set<UUID> ids) {
        return userRepository.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(
                        User::getId,
                        u -> u.getFullName() != null
                                ? u.getFullName()
                                : u.getEmail()
                ));
    }
}