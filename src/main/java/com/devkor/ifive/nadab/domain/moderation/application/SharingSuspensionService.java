package com.devkor.ifive.nadab.domain.moderation.application;

import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.domain.moderation.api.dto.response.SuspensionStatusResponse;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import com.devkor.ifive.nadab.domain.moderation.core.entity.SocialSuspension;
import com.devkor.ifive.nadab.domain.moderation.core.repository.ContentReportRepository;
import com.devkor.ifive.nadab.domain.moderation.core.repository.SocialSuspensionRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SharingSuspensionService {

    private static final long REPORT_COUNT_THRESHOLD = 20L;
    private static final long REPORTER_COUNT_THRESHOLD = 3L;
    static final long SUSPENSION_HOURS = 720L;

    private final ContentReportRepository contentReportRepository;
    private final SocialSuspensionRepository socialSuspensionRepository;
    private final DailyReportRepository dailyReportRepository;
    private final UserRepository userRepository;

    public boolean isSharingSuspended(Long userId) {
        return socialSuspensionRepository.existsByUserIdAndExpiresAtAfter(userId, OffsetDateTime.now());
    }

    public List<Long> getSharingSuspendedUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return socialSuspensionRepository.findActiveSuspendedUserIds(userIds, OffsetDateTime.now());
    }

    public SuspensionStatusResponse getSuspensionStatus(Long userId) {
        OffsetDateTime now = OffsetDateTime.now();
        return socialSuspensionRepository.findFirstByUserIdOrderByStartedAtDesc(userId)
                .filter(s -> s.getExpiresAt().isAfter(now))
                .map(s -> SuspensionStatusResponse.suspended(s.getExpiresAt()))
                .orElse(SuspensionStatusResponse.notSuspended());
    }

    public List<Long> getAllActiveSuspendedUserIds() {
        return socialSuspensionRepository.findAllActiveSuspendedUserIds(OffsetDateTime.now());
    }

    /**
     * 신고 저장 후 호출. 정지 조건(친구 신고 20건 이상 + 신고자 3명 이상) 충족 시 정지 발동.
     * 정지 중이면 조건 체크를 건너뜀.
     */
    @Transactional
    public void checkAndTriggerSuspension(Long reportedUserId) {
        if (isSharingSuspended(reportedUserId)) {
            return;
        }

        // 가장 최근 정지의 expires_at을 기준점으로 사용 (null이면 전체 누적)
        OffsetDateTime since = socialSuspensionRepository
                .findFirstByUserIdOrderByStartedAtDesc(reportedUserId)
                .map(SocialSuspension::getExpiresAt)
                .orElse(null);

        long reportCount = contentReportRepository.countReportsSince(reportedUserId, since);
        if (reportCount < REPORT_COUNT_THRESHOLD) {
            return;
        }

        long reporterCount = contentReportRepository.countDistinctReportersSince(reportedUserId, since);
        if (reporterCount < REPORTER_COUNT_THRESHOLD) {
            return;
        }

        // 정지 발동
        OffsetDateTime now = OffsetDateTime.now();
        User user = userRepository.getReferenceById(reportedUserId);
        socialSuspensionRepository.save(SocialSuspension.create(user, now, now.plusHours(SUSPENSION_HOURS)));
        dailyReportRepository.stopSharingByUserIdAndDate(reportedUserId, TodayDateTimeProvider.getTodayDate());
    }
}