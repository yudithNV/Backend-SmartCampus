package com.example.smartcampus.repository;

import com.example.smartcampus.entity.CommentReport;
import com.example.smartcampus.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {

    boolean existsByCommentIdAndReporterId(Long commentId, UUID reporterId);

    long countByCommentIdAndStatus(Long commentId, ReportStatus status);

    List<CommentReport> findAllByStatusOrderByCreatedAtDesc(ReportStatus status);

    @Query("""
        SELECT r FROM CommentReport r
        WHERE r.commentId IN :commentIds
          AND r.reporterId = :userId
    """)
    List<CommentReport> findByCommentIdInAndReporterId(
        @Param("commentIds") List<Long> commentIds,
        @Param("userId") UUID userId
    );

    @Query("""
        SELECT r FROM CommentReport r
        WHERE r.status = 'PENDIENTE'
        ORDER BY r.createdAt DESC
    """)
    List<CommentReport> findAllPending();
}