package com.devkor.ifive.nadab.domain.friend.application;

import com.devkor.ifive.nadab.domain.friend.api.dto.response.SearchHistoryListResponse;
import com.devkor.ifive.nadab.domain.friend.api.dto.response.SearchHistoryResponse;
import com.devkor.ifive.nadab.domain.friend.core.entity.FriendSearchHistory;
import com.devkor.ifive.nadab.domain.friend.core.repository.FriendSearchHistoryRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.infra.ProfileImageUrlBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendSearchHistoryQueryService {

    private final FriendSearchHistoryRepository friendSearchHistoryRepository;
    private final ProfileImageUrlBuilder profileImageUrlBuilder;

    public SearchHistoryListResponse getRecentSearches(Long userId) {
        List<FriendSearchHistory> histories = friendSearchHistoryRepository.findTop5ByUser_IdOrderByUpdatedAtDesc(userId);

        List<SearchHistoryResponse> items = histories.stream()
                .map(h -> {
                    User searchedUser = h.getSearchedUser();
                    String profileImageUrl = profileImageUrlBuilder.buildUserProfileUrl(searchedUser);
                    return new SearchHistoryResponse(
                            searchedUser.getNickname(),
                            profileImageUrl
                    );
                })
                .toList();

        return new SearchHistoryListResponse(items);
    }
}