package com.devkor.ifive.nadab.domain.moderation.application;

import com.devkor.ifive.nadab.domain.moderation.core.repository.ContentReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 공유 활동 중지 판단 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SharingSuspensionService {

    private static final long REPORT_COUNT_THRESHOLD = 10L;
    private static final long REPORTER_COUNT_THRESHOLD = 2L;

    private final ContentReportRepository contentReportRepository;

    /**
     * 특정 유저가 공유 활동 중지 상태인지 확인 (신고 10건 이상 && 신고자 2명 이상)
     */
    public boolean isSharingSuspended(Long userId) {
        long reportCount = contentReportRepository.countByReportedUserId(userId);
        if (reportCount < REPORT_COUNT_THRESHOLD) {
            return false;
        }

        long reporterCount = contentReportRepository.countDistinctReportersByReportedUserId(userId);
        return reporterCount >= REPORTER_COUNT_THRESHOLD;
    }

    /**
     * 여러 유저 중 공유 활동 중지된 유저 ID 조회
     */
    public List<Long> getSharingSuspendedUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return contentReportRepository.findSharingSuspendedUserIds(userIds);
    }
}