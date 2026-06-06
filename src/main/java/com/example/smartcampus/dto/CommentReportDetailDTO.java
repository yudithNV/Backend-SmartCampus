package com.example.smartcampus.dto;

import com.example.smartcampus.entity.ReportReason;
import com.example.smartcampus.entity.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentReportDetailDTO {
    private Long           id;
    private Long           commentId;
    private UUID           reporterId;
    private String         reporterName;       // nombre completo del reportador
    private ReportReason   reason;
    private String         description;
    private ReportStatus   status;
    private OffsetDateTime createdAt;
    private String         commentAuthorName;  // autor del comentario (para el modal)
}