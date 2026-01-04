package com.devkor.ifive.nadab.domain.search.application;

import com.devkor.ifive.nadab.domain.search.core.entity.SearchHistory;
import com.devkor.ifive.nadab.domain.search.core.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchHistoryQueryService {

    private final SearchHistoryRepository searchHistoryRepository;

    public List<SearchHistory> getRecentSearches(Long userId) {
        return searchHistoryRepository.findTop10ByUserIdOrderByUpdatedAtDesc(userId);
    }
}