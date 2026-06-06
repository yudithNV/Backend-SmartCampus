package com.example.smartcampus.dto;

import com.example.smartcampus.entity.ReportReason;
import com.example.smartcampus.entity.ReportStatus;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PublisherCommentReportGroupDTO {
    private Long         latestReportId;   // ID del reporte más reciente (para acciones)
    private Long         commentId;
    private String       commentBody;
    private Long         newsId;
    private String       newsTitle;
    private String       reporterName;     // nombre del último reportador
    private ReportStatus status;
    private OffsetDateTime createdAt;
    private long         totalReportsForComment;
    // PA 5: lista de TODOS los motivos únicos del comentario
    private List<ReasonEntry> reasons;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ReasonEntry {
        private ReportReason reason;
        private String       description;
    }
}