package com.devkor.ifive.nadab.domain.moderation.application;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.domain.moderation.core.entity.ContentReport;
import com.devkor.ifive.nadab.domain.moderation.core.entity.ReportReason;
import com.devkor.ifive.nadab.domain.moderation.core.repository.ContentReportRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ContentReportCommandService {

    private final ContentReportRepository contentReportRepository;
    private final DailyReportRepository dailyReportRepository;
    private final UserRepository userRepository;

    public void reportContent(Long reporterId, Long dailyReportId, ReportReason reason, String customReason) {
        // 1. customReason 검증
        validateCustomReason(reason, customReason);

        // 2. 중복 신고 검증
        if (contentReportRepository.existsByReporterIdAndDailyReportId(reporterId, dailyReportId)) {
            throw new ConflictException(ErrorCode.CONTENT_REPORT_ALREADY_EXISTS);
        }

        // 3. DailyReport 조회
        DailyReport dailyReport = dailyReportRepository.findById(dailyReportId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DAILY_REPORT_NOT_FOUND));

        // 4. 자기 신고 방지 검증
        User reportedUser = dailyReport.getAnswerEntry().getUser();
        if (reporterId.equals(reportedUser.getId())) {
            throw new BadRequestException(ErrorCode.CONTENT_REPORT_SELF_REPORT_FORBIDDEN);
        }

        // 5. 신고 저장 (동시성 예외 처리)
        User reporter = userRepository.getReferenceById(reporterId);

        ContentReport report = ContentReport.create(
                reporter, dailyReport, reportedUser, reason, customReason
        );

        try {
            contentReportRepository.save(report);
        } catch (DataIntegrityViolationException e) {
            // UNIQUE 제약 위반인지 재조회로 확인
            boolean isDuplicate = contentReportRepository
                    .existsByReporterIdAndDailyReportId(reporterId, dailyReportId);

            if (isDuplicate) {
                throw new ConflictException(ErrorCode.CONTENT_REPORT_ALREADY_EXISTS);
            }

            throw e;
        }
    }

    private void validateCustomReason(ReportReason reason, String customReason) {
        if (reason == ReportReason.OTHER) {
            if (customReason == null || customReason.isBlank()) {
                throw new BadRequestException(ErrorCode.CONTENT_REPORT_INVALID);
            }
            if (customReason.length() > 200) {
                throw new BadRequestException(ErrorCode.CONTENT_REPORT_INVALID);
            }
        }
    }
}