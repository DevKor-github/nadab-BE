package com.devkor.ifive.nadab.domain.friend.application;

import com.devkor.ifive.nadab.domain.friend.core.entity.FriendSearchHistory;
import com.devkor.ifive.nadab.domain.friend.core.repository.FriendSearchHistoryRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FriendSearchHistoryCommandService {

    private final FriendSearchHistoryRepository friendSearchHistoryRepository;
    private final UserRepository userRepository;

    public void saveSearchHistoryByNickname(Long userId, String nickname) {
        try {
            // 1. 닉네임으로 유저 조회
            User searchedUser = userRepository.findByNicknameAndDeletedAtIsNull(nickname)
                    .orElse(null);
            if (searchedUser == null) {
                return; // 유저 없으면 종료
            }

            Long searchedUserId = searchedUser.getId();

            // 2. 기존 검색 기록 찾기
            Optional<FriendSearchHistory> existing = friendSearchHistoryRepository
                    .findByUserIdAndSearchedUserId(userId, searchedUserId);

            if (existing.isPresent()) {
                // 이미 존재하면 updated_at만 갱신
                friendSearchHistoryRepository.updateTimestamp(existing.get().getId());
            } else {
                // 없으면 새로 생성
                User user = userRepository.getReferenceById(userId);
                FriendSearchHistory newHistory = FriendSearchHistory.create(user, searchedUser);
                friendSearchHistoryRepository.save(newHistory);
            }

            // 3. 100개 초과 시에만 오래된 것 벌크 삭제
            Long count = friendSearchHistoryRepository.countByUserId(userId);
            if (count > 100) {
                int deletedCount = friendSearchHistoryRepository.deleteOldHistoriesExceedingLimit(userId);
                log.debug("오래된 검색 기록 {}개 삭제 - userId: {}", deletedCount, userId);
            }

        } catch (Exception e) {
            // 검색 기록은 실패해도 예외를 던지지 않아서 검색 결과 조회에는 영향을 주지 않도록 처리
            log.error("친구 검색 기록 저장 실패 - userId: {}, nickname: {}", userId, nickname, e);
        }
    }

    public void deleteSearchHistoryByNickname(Long userId, String nickname) {
        userRepository.findByNicknameAndDeletedAtIsNull(nickname)
                .ifPresent(searchedUser ->
                    friendSearchHistoryRepository.deleteByUserIdAndSearchedUserId(userId, searchedUser.getId())
                );
    }

    public void deleteAllSearchHistories(Long userId) {
        friendSearchHistoryRepository.deleteAllByUserId(userId);
    }
}