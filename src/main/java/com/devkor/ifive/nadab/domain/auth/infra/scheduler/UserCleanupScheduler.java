package com.devkor.ifive.nadab.domain.auth.infra.scheduler;

import com.devkor.ifive.nadab.domain.dailyreport.core.repository.AnswerEntryRepository;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.user.core.service.ProfileImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private final AnswerEntryRepository answerEntryRepository;
    private final ProfileImageService profileImageService;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    @Transactional
    public void cleanupDeletedUsers() {
        OffsetDateTime expirationDate = OffsetDateTime.now().minusDays(14);

        // 영구 삭제될 User의 id들
        List<Long> targetUserIds = userRepository.findOldWithdrawnUserIds(expirationDate);

        if(targetUserIds.isEmpty()) {
            log.debug("정리할 탈퇴 회원이 없습니다.");
            return;
        }

        // 삭제 대상 회원들의 프로필 이미지 키와 답변 이미지 키를 모두 수집하여 S3에서 삭제
        Set<String> imageKeysToDelete = new HashSet<>();
        imageKeysToDelete.addAll(userRepository.findProfileImageKeysByIdIn(targetUserIds));
        imageKeysToDelete.addAll(answerEntryRepository.findImageKeysByUserIds(targetUserIds));
        imageKeysToDelete.forEach(profileImageService::deleteProfileImage);

        int deletedCount = userRepository.deleteWithdrawnUsersByIdIn(targetUserIds);

        if (deletedCount > 0) {
            log.info("회원 정리 완료: {}명의 탈퇴 회원 영구 삭제 (기준일: {})", deletedCount, expirationDate.toLocalDate());
        }
    }
}