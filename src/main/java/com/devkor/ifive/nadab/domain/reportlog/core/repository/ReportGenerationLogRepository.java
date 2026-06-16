package com.devkor.ifive.nadab.domain.reportlog.core.repository;

import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationLog;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationLogStatus;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportGenerationLogRepository extends JpaRepository<ReportGenerationLog, Long> {

    List<ReportGenerationLog> findAllByReportTypeAndReportIdOrderByCreatedAtDesc(
            ReportGenerationType reportType,
            Long reportId
    );

    List<ReportGenerationLog> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    List<ReportGenerationLog> findAllByStatusOrderByCreatedAtDesc(ReportGenerationLogStatus status);
}
