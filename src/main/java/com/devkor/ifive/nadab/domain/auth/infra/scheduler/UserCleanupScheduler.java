package com.devkor.ifive.nadab.domain.auth.infra.scheduler;

import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * 탈퇴 유저 영구 삭제 스케줄러
 * - 매일 자정(00시)에 실행
 * - 탈퇴 후 14일 경과한 회원 데이터 영구 삭제
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    @Transactional
    public void cleanupDeletedUsers() {
        OffsetDateTime expirationDate = OffsetDateTime.now().minusDays(14);
        int deletedCount = userRepository.deleteOldWithdrawnUsers(expirationDate);

        if (deletedCount > 0) {
            log.info("회원 정리 완료: {}명의 탈퇴 회원 영구 삭제 (기준일: {})", deletedCount, expirationDate.toLocalDate());
        } else {
            log.debug("정리할 탈퇴 회원이 없습니다.");
        }
    }
}