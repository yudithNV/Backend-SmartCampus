package com.example.smartcampus.service;

import com.example.smartcampus.dto.CommentReportRequestDTO;
import com.example.smartcampus.dto.CommentReportResponseDTO;
import com.example.smartcampus.entity.*;
import com.example.smartcampus.repository.CommentReportRepository;
import com.example.smartcampus.repository.NewsCommentRepository;
import exception.ForbiddenException;
import exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentReportService {

    private static final long AUTO_HIDE_THRESHOLD = 5L;

    private final CommentReportRepository reportRepository;
    private final NewsCommentRepository   commentRepository;

    @Transactional
    public CommentReportResponseDTO reportComment(
            Long newsId,
            Long commentId,
            CommentReportRequestDTO dto,
            User reporter) {

        NewsComment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new NotFoundException("Comentario no encontrado"));

        if (!comment.getNewsId().equals(newsId)) {
            throw new NotFoundException("Comentario no pertenece a esta noticia");
        }

        // PA SCRUM-436/470: un usuario no puede reportar el mismo comentario más de una vez
        if (reportRepository.existsByCommentIdAndReporterId(commentId, reporter.getId())) {
            long total = reportRepository.countByCommentIdAndStatus(commentId, ReportStatus.PENDIENTE);
            return CommentReportResponseDTO.builder()
                .commentId(commentId)
                .reporterId(reporter.getId())
                .reason(dto.getReason())
                .status(ReportStatus.PENDIENTE)
                .alreadyReported(true)
                .totalReports(total)
                .build();
        }

        CommentReport report = CommentReport.builder()
            .commentId(commentId)
            .reporterId(reporter.getId())
            .reason(dto.getReason())
            .description(dto.getDescription())
            .status(ReportStatus.PENDIENTE)
            .build();

        CommentReport saved = reportRepository.save(report);

        // PA SCRUM-438/468: auto-ocultar al llegar a 5 reportes únicos
        long totalPending = reportRepository.countByCommentIdAndStatus(commentId, ReportStatus.PENDIENTE);
        if (totalPending >= AUTO_HIDE_THRESHOLD && !comment.getHidden()) {
            comment.setHidden(true);
            commentRepository.save(comment);
        }

        return toDTO(saved, false, totalPending);
    }

    @Transactional(readOnly = true)
    public boolean hasReported(Long commentId, User user) {
        return reportRepository.existsByCommentIdAndReporterId(commentId, user.getId());
    }

    // ── Mapper ────────────────────────────────────────────────────
    private CommentReportResponseDTO toDTO(CommentReport r, boolean alreadyReported, long total) {
        return CommentReportResponseDTO.builder()
            .id(r.getId())
            .commentId(r.getCommentId())
            .reporterId(r.getReporterId())
            .reason(r.getReason())
            .description(r.getDescription())
            .status(r.getStatus())
            .createdAt(r.getCreatedAt())
            .alreadyReported(alreadyReported)
            .totalReports(total)
            .build();
    }
}