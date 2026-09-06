package com.devkor.ifive.nadab.domain.auth.infra.scheduler;

import com.devkor.ifive.nadab.domain.auth.core.repository.UserWithdrawalReasonRepository;
import com.devkor.ifive.nadab.domain.auth.core.repository.WithdrawalEventReasonRepository;
import com.devkor.ifive.nadab.domain.auth.core.repository.WithdrawalEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalEventCleanupScheduler {

    private static final int ACCOUNT_RECOVERY_PERIOD_DAYS = 14;

    private final WithdrawalEventRepository withdrawalEventRepository;
    private final WithdrawalEventReasonRepository withdrawalEventReasonRepository;
    private final UserWithdrawalReasonRepository userWithdrawalReasonRepository;

    @Scheduled(cron = "0 10 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void cleanupWithdrawalEvents() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime anonymizationDate = now.minusDays(ACCOUNT_RECOVERY_PERIOD_DAYS);

        int deletedEventCount = withdrawalEventRepository.deleteExpiredEvents(now);
        int deletedLegacyReasonCount = userWithdrawalReasonRepository
                .deleteReasonsWithdrawnBefore(anonymizationDate);
        int clearedCustomReasonCount = withdrawalEventReasonRepository
                .clearCustomReasonsWithdrawnBefore(anonymizationDate);
        int anonymizedEventCount = withdrawalEventRepository
                .anonymizeEventsWithdrawnBefore(anonymizationDate, now);

        if (deletedEventCount == 0
                && deletedLegacyReasonCount == 0
                && clearedCustomReasonCount == 0
                && anonymizedEventCount == 0) {
            log.debug("정리할 탈퇴 이벤트가 없습니다.");
            return;
        }

        log.info(
                "탈퇴 이벤트 정리 완료: 만료 이벤트 {}건 삭제, 레거시 사유 {}건 삭제, "
                        + "자유 입력 {}건 제거, 이벤트 {}건 비식별화",
                deletedEventCount,
                deletedLegacyReasonCount,
                clearedCustomReasonCount,
                anonymizedEventCount
        );
    }
}
