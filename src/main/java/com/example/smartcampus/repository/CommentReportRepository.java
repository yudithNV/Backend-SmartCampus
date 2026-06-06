package com.example.smartcampus.repository;

import com.example.smartcampus.entity.CommentReport;
import com.example.smartcampus.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;
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

    @Query("SELECT r FROM CommentReport r WHERE r.commentId IN :commentIds AND r.status = :status ORDER BY r.createdAt DESC")
    List<CommentReport> findByCommentIdInAndStatus(
        @Param("commentIds") List<Long> commentIds,
        @Param("status") ReportStatus status);

    @Query("SELECT r FROM CommentReport r WHERE r.commentId IN :commentIds ORDER BY r.createdAt DESC")
    List<CommentReport> findByCommentIdIn(@Param("commentIds") List<Long> commentIds);

    @Query("SELECT r.commentId, COUNT(r) FROM CommentReport r WHERE r.commentId IN :commentIds GROUP BY r.commentId")
    List<Object[]> countGroupedByCommentIdRaw(@Param("commentIds") List<Long> commentIds);

    default Map<Long, Long> countGroupedByCommentId(List<Long> ids) {
        return countGroupedByCommentIdRaw(ids).stream()
                .collect(java.util.stream.Collectors.toMap(
                    row -> (Long) row[0],
                    row -> (Long) row[1]
                ));
    }

    long countByCommentIdInAndStatus(List<Long> commentIds, ReportStatus status);
    List<CommentReport> findByCommentIdOrderByCreatedAtDesc(Long commentId);
}