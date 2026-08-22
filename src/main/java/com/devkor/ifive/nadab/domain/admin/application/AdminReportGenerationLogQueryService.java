package com.devkor.ifive.nadab.domain.admin.application;

import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminLogPageResponse;
import com.devkor.ifive.nadab.domain.admin.api.dto.response.AdminReportGenerationLogResponse;
import com.devkor.ifive.nadab.domain.reportlog.core.repository.ReportGenerationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportGenerationLogQueryService {

    private final ReportGenerationLogRepository reportGenerationLogRepository;

    public AdminLogPageResponse<AdminReportGenerationLogResponse> getLogs(
            AdminLogSearchCondition condition
    ) {
        return AdminLogPageResponse.from(
                reportGenerationLogRepository.findAllForAdmin(
                                condition.nickname(),
                                condition.email(),
                                condition.toPageable()
                        )
                        .map(AdminReportGenerationLogResponse::from)
        );
    }
}
