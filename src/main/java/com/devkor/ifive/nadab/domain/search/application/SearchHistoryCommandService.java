package com.devkor.ifive.nadab.domain.search.application;

import com.devkor.ifive.nadab.domain.search.core.entity.SearchHistory;
import com.devkor.ifive.nadab.domain.search.core.repository.SearchHistoryRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.exception.ForbiddenException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SearchHistoryCommandService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;

    public void saveOrRefreshSearchHistory(Long userId, String keyword) {
        try {
            // 키워드가 없으면 저장 안 함
            if (!StringUtils.hasText(keyword)) {
                return;
            }

            // 양 끝 공백 제거 (중간 공백은 유지)
            String trimmedKeyword = keyword.trim();

            // 기존 검색어 찾기
            Optional<SearchHistory> existing = searchHistoryRepository
                    .findByUserIdAndKeyword(userId, trimmedKeyword);

            if (existing.isPresent()) {
                // 존재하면 updated_at만 갱신
                searchHistoryRepository.updateTimestamp(existing.get().getId());
            } else {
                // 없으면 새로 생성
                User user = userRepository.getReferenceById(userId);
                SearchHistory newHistory = SearchHistory.create(user, trimmedKeyword);
                searchHistoryRepository.save(newHistory);
            }

            // 100개 초과 시 오래된 것 삭제
            Long count = searchHistoryRepository.countByUserId(userId);
            if (count > 100) {
                List<SearchHistory> allHistories = searchHistoryRepository
                        .findAllByUserIdOrderByUpdatedAtDesc(userId);
                List<SearchHistory> historiesToDelete = allHistories.subList(100, allHistories.size());
                searchHistoryRepository.deleteAll(historiesToDelete);
            }
        } catch (Exception e) {
            // 검색어 기록은 실패해도 예외를 던지지 않아서 검색 결과 조회에는 영향을 주지 않도록 처리
            log.error("검색어 히스토리 저장 실패 - userId: {}, keyword: {}", userId, keyword, e);
        }
    }

    public void deleteSearchHistory(Long userId, Long historyId) {
        // 검색어 존재 확인
        SearchHistory history = searchHistoryRepository.findById(historyId)
                .orElseThrow(() -> new NotFoundException("검색어를 찾을 수 없습니다"));

        // 권한 확인
        if (!history.getUser().getId().equals(userId)) {
            throw new ForbiddenException("본인의 검색어만 삭제할 수 있습니다");
        }

        // 삭제
        searchHistoryRepository.deleteById(historyId);
    }

    public void deleteAllSearchHistories(Long userId) {
        searchHistoryRepository.deleteAllByUserId(userId);
    }
}