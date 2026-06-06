package com.example.smartcampus.service;

import com.example.smartcampus.dto.CommentReportDetailDTO;
import com.example.smartcampus.dto.PublisherCommentReportGroupDTO;
import com.example.smartcampus.dto.PublisherReportSummaryDTO;
import com.example.smartcampus.entity.CommentReport;
import com.example.smartcampus.entity.News;
import com.example.smartcampus.entity.NewsComment;
import com.example.smartcampus.entity.ReportStatus;
import com.example.smartcampus.entity.Role;
import com.example.smartcampus.entity.User;
import com.example.smartcampus.repository.CommentReportRepository;
import com.example.smartcampus.repository.NewsCommentRepository;
import com.example.smartcampus.repository.NewsRepository;
import com.example.smartcampus.repository.UserRepository;
import exception.ForbiddenException;
import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublisherReportService {

    private final CommentReportRepository reportRepository;
    private final NewsCommentRepository commentRepository;
    private final NewsRepository newsRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PublisherCommentReportGroupDTO> getReportsForPublisher(
            User publisher,
            ReportStatus status) {

        validatePublisher(publisher);

        List<News> myNews = newsRepository
                .findAllByAuthorIdOrderByCreatedAtDesc(publisher.getId());

        if (myNews.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> myNewsIds = myNews.stream()
                .map(News::getId)
                .collect(Collectors.toSet());

        List<Long> myCommentIds = commentRepository
                .findCommentIdsByNewsIds(new ArrayList<>(myNewsIds));

        if (myCommentIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<CommentReport> reports = status != null
                ? reportRepository.findByCommentIdInAndStatus(
                        myCommentIds,
                        status
                )
                : reportRepository.findByCommentIdIn(myCommentIds);

        if (reports.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, NewsComment> commentsMap = commentRepository
                .findAllById(
                        reports.stream()
                                .map(CommentReport::getCommentId)
                                .collect(Collectors.toList())
                )
                .stream()
                .collect(Collectors.toMap(
                        NewsComment::getId,
                        comment -> comment
                ));

        Map<Long, News> newsMap = myNews.stream()
                .collect(Collectors.toMap(
                        News::getId,
                        news -> news
                ));

        Map<UUID, String> userNames = loadUserNames(
                reports.stream()
                        .map(CommentReport::getReporterId)
                        .collect(Collectors.toSet())
        );

        Map<Long, Long> totalByComment =
                reportRepository.countGroupedByCommentId(myCommentIds);

        Map<Long, PublisherCommentReportGroupDTO> grouped =
                new LinkedHashMap<>();

        for (CommentReport report : reports) {

            Long commentId = report.getCommentId();

            NewsComment comment = commentsMap.get(commentId);
            Long newsId = comment != null
                    ? comment.getNewsId()
                    : null;

            News news = newsId != null
                    ? newsMap.get(newsId)
                    : null;

            PublisherCommentReportGroupDTO group =
                    grouped.get(commentId);

            if (group == null) {

                group = PublisherCommentReportGroupDTO.builder()
                        .latestReportId(report.getId())
                        .commentId(commentId)
                        .commentBody(
                                comment != null
                                        ? comment.getBody()
                                        : ""
                        )
                        .newsId(newsId)
                        .newsTitle(
                                news != null
                                        ? news.getTitle()
                                        : ""
                        )
                        .reporterName(
                                userNames.getOrDefault(
                                        report.getReporterId(),
                                        "Usuario"
                                )
                        )
                        .status(report.getStatus())
                        .createdAt(report.getCreatedAt())
                        .totalReportsForComment(
                                totalByComment.getOrDefault(
                                        commentId,
                                        1L
                                )
                        )
                        .reasons(new ArrayList<>())
                        .build();

                grouped.put(commentId, group);
            }

            boolean alreadyHasReason = group.getReasons()
                    .stream()
                    .anyMatch(reasonEntry ->
                            reasonEntry.getReason()
                                    == report.getReason());

            if (!alreadyHasReason) {
                group.getReasons().add(
                        PublisherCommentReportGroupDTO
                                .ReasonEntry
                                .builder()
                                .reason(report.getReason())
                                .description(report.getDescription())
                                .build()
                );
            }

            if (report.getCreatedAt()
                    .isAfter(group.getCreatedAt())) {

                group.setLatestReportId(report.getId());
                group.setCreatedAt(report.getCreatedAt());
                group.setReporterName(
                        userNames.getOrDefault(
                                report.getReporterId(),
                                "Usuario"
                        )
                );
            }
        }

        return new ArrayList<>(grouped.values());
    }

    @Transactional(readOnly = true)
        public List<CommentReportDetailDTO> getCommentReportsDetail(
                Long commentId,
                User publisher) {

        validatePublisher(publisher);

        NewsComment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Comentario no encontrado"
                        ));

        News news = newsRepository.findById(comment.getNewsId())
                .orElseThrow(() ->
                        new NotFoundException(
                                "Noticia no encontrada"
                        ));

        if (!news.getAuthorId().equals(publisher.getId())) {
                throw new ForbiddenException(
                        "No tienes permiso para ver estos reportes"
                );
        }

        String commentAuthorName = userRepository
                .findById(comment.getUserId())
                .map(user -> {

                        String fullName = user.getFullName();

                        if (fullName == null || fullName.isBlank()) {
                        return user.getEmail();
                        }

                        return fullName;
                })
                .orElse("Usuario");

        List<CommentReport> reports = reportRepository
                .findByCommentIdOrderByCreatedAtDesc(commentId);

        if (reports.isEmpty()) {
                return Collections.emptyList();
        }

        Set<UUID> reporterIds = reports.stream()
                .map(CommentReport::getReporterId)
                .collect(Collectors.toSet());

        Map<UUID, String> reporterNames = userRepository
                .findAllById(reporterIds)
                .stream()
                .collect(Collectors.toMap(
                        User::getId,
                        user -> {

                                String fullName = user.getFullName();

                                if (fullName == null || fullName.isBlank()) {
                                return user.getEmail();
                                }

                                return fullName;
                        }
                ));

        return reports.stream()
                .map(report -> CommentReportDetailDTO.builder()
                        .id(report.getId())
                        .commentId(report.getCommentId())
                        .reporterId(report.getReporterId())
                        .reporterName(
                                reporterNames.getOrDefault(
                                        report.getReporterId(),
                                        "Usuario"
                                )
                        )
                        .reason(report.getReason())
                        .description(report.getDescription())
                        .status(report.getStatus())
                        .createdAt(report.getCreatedAt())
                        .commentAuthorName(commentAuthorName)
                        .build())
                .collect(Collectors.toList());
        }

    @Transactional(readOnly = true)
    public PublisherReportSummaryDTO getSummary(User publisher) {

        validatePublisher(publisher);

        List<News> myNews = newsRepository
                .findAllByAuthorIdOrderByCreatedAtDesc(
                        publisher.getId()
                );

        if (myNews.isEmpty()) {
            return PublisherReportSummaryDTO.builder()
                    .totalPending(0)
                    .pendingByNewsId(Collections.emptyMap())
                    .build();
        }

        Set<Long> myNewsIds = myNews.stream()
                .map(News::getId)
                .collect(Collectors.toSet());

        List<Long> myCommentIds = commentRepository
                .findCommentIdsByNewsIds(
                        new ArrayList<>(myNewsIds)
                );

        if (myCommentIds.isEmpty()) {
            return PublisherReportSummaryDTO.builder()
                    .totalPending(0)
                    .pendingByNewsId(Collections.emptyMap())
                    .build();
        }

        long total = reportRepository
                .countByCommentIdInAndStatus(
                        myCommentIds,
                        ReportStatus.PENDIENTE
                );

        Map<Long, Long> pendingByNews = new HashMap<>();

        List<CommentReport> pending = reportRepository
                .findByCommentIdInAndStatus(
                        myCommentIds,
                        ReportStatus.PENDIENTE
                );

        Map<Long, Long> commentToNews =
                commentRepository.findAllById(myCommentIds)
                        .stream()
                        .collect(Collectors.toMap(
                                NewsComment::getId,
                                NewsComment::getNewsId
                        ));

        pending.forEach(report -> {
            Long newsId =
                    commentToNews.get(report.getCommentId());

            if (newsId != null) {
                pendingByNews.merge(
                        newsId,
                        1L,
                        Long::sum
                );
            }
        });

        return PublisherReportSummaryDTO.builder()
                .totalPending(total)
                .pendingByNewsId(pendingByNews)
                .build();
    }

    private void validatePublisher(User user) {

        if (user.getRole() != Role.PUBLICADOR
                && user.getRole() != Role.ADMINISTRADOR) {

            throw new ForbiddenException(
                    "Solo los publicadores pueden acceder a esta sección."
            );
        }
    }

    private Map<UUID, String> loadUserNames(Set<UUID> ids) {

        return userRepository.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(
                        User::getId,
                        user -> user.getFullName() != null
                                ? user.getFullName()
                                : user.getEmail()
                ));
    }
}