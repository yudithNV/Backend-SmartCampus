package com.example.smartcampus.service;

import com.example.smartcampus.dto.ModerationActionDTO;
import com.example.smartcampus.dto.ModerationReportDTO;
import com.example.smartcampus.entity.*;
import com.example.smartcampus.repository.*;
import exception.ForbiddenException;
import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModerationService {

    private final CommentReportRepository reportRepository;
    private final NewsCommentRepository   commentRepository;
    private final NewsRepository          newsRepository;
    private final UserRepository          userRepository;

    @Transactional(readOnly = true)
    public List<ModerationReportDTO> getPendingReports(User moderator) {
        requireModerator(moderator);
        List<CommentReport> reports = reportRepository.findAllPending();
        return buildDTOs(reports);
    }

    @Transactional
    public ModerationReportDTO processReport(Long reportId, ModerationActionDTO dto, User moderator) {
        requireModerator(moderator);

        if (dto.getAction() == ReportStatus.PENDIENTE) {
            throw new IllegalArgumentException("Acción inválida: PENDIENTE no es una acción de moderación");
        }

        CommentReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new NotFoundException("Reporte no encontrado"));

        report.setStatus(dto.getAction());
        report.setReviewedAt(OffsetDateTime.now());
        reportRepository.save(report);

        // SCRUM-435: aplicar acción sobre el comentario
        NewsComment comment = commentRepository.findById(report.getCommentId())
            .orElseThrow(() -> new NotFoundException("Comentario no encontrado"));

        switch (dto.getAction()) {
            case OCULTO    -> { comment.setHidden(true);  commentRepository.save(comment); }
            case ELIMINADO -> commentRepository.deleteById(comment.getId());
            case IGNORADO  -> { comment.setHidden(false); commentRepository.save(comment); }
            default        -> {}
        }

        return buildDTO(report);
    }

    // ── Helpers ───────────────────────────────────────────────────
    private void requireModerator(User user) {
        if (user.getRole() != Role.PUBLICADOR && user.getRole() != Role.ADMINISTRADOR) {
            throw new ForbiddenException("Solo moderadores pueden acceder a este panel");
        }
    }

    private List<ModerationReportDTO> buildDTOs(List<CommentReport> reports) {
        return reports.stream().map(this::buildDTO).collect(Collectors.toList());
    }

    private ModerationReportDTO buildDTO(CommentReport r) {
        NewsComment comment = commentRepository.findById(r.getCommentId()).orElse(null);
        News news = comment != null ? newsRepository.findById(comment.getNewsId()).orElse(null) : null;
        User reporter = userRepository.findById(r.getReporterId()).orElse(null);
        long totalForComment = reportRepository.countByCommentIdAndStatus(r.getCommentId(), ReportStatus.PENDIENTE);

        return ModerationReportDTO.builder()
            .id(r.getId())
            .commentId(r.getCommentId())
            .commentBody(comment != null ? comment.getBody() : "[eliminado]")
            .newsId(news != null ? news.getId() : null)
            .newsTitle(news != null ? news.getTitle() : null)
            .reporterId(r.getReporterId())
            .reporterName(reporter != null ? reporter.getFullName() : "Usuario")
            .reason(r.getReason())
            .description(r.getDescription())
            .status(r.getStatus())
            .createdAt(r.getCreatedAt())
            .totalReportsForComment(totalForComment)
            .build();
    }
}