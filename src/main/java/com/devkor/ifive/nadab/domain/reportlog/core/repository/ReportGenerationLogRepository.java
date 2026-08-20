package com.devkor.ifive.nadab.domain.reportlog.core.repository;

import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationLog;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationLogStatus;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportGenerationLogRepository extends JpaRepository<ReportGenerationLog, Long> {

    List<ReportGenerationLog> findAllByReportTypeAndReportIdOrderByCreatedAtDesc(
            ReportGenerationType reportType,
            Long reportId
    );

    List<ReportGenerationLog> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    List<ReportGenerationLog> findAllByStatusOrderByCreatedAtDesc(ReportGenerationLogStatus status);

    @Query(value = """
            SELECT l
            FROM ReportGenerationLog l
            LEFT JOIN FETCH l.user u
            WHERE (COALESCE(:nickname, '') = ''
                   OR LOWER(u.nickname) LIKE CONCAT('%', COALESCE(:nickname, ''), '%'))
              AND (COALESCE(:email, '') = ''
                   OR LOWER(u.email) LIKE CONCAT('%', COALESCE(:email, ''), '%'))
            ORDER BY l.createdAt DESC, l.id DESC
            """,
            countQuery = """
            SELECT COUNT(l)
            FROM ReportGenerationLog l
            LEFT JOIN l.user u
            WHERE (COALESCE(:nickname, '') = ''
                   OR LOWER(u.nickname) LIKE CONCAT('%', COALESCE(:nickname, ''), '%'))
              AND (COALESCE(:email, '') = ''
                   OR LOWER(u.email) LIKE CONCAT('%', COALESCE(:email, ''), '%'))
            """)
    Page<ReportGenerationLog> findAllForAdmin(
            @Param("nickname") String nickname,
            @Param("email") String email,
            Pageable pageable
    );
}
