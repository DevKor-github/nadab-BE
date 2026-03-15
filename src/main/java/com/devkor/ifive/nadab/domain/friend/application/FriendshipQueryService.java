package com.devkor.ifive.nadab.domain.friend.application;

import com.devkor.ifive.nadab.domain.friend.api.dto.response.*;
import com.devkor.ifive.nadab.domain.friend.core.entity.Friendship;
import com.devkor.ifive.nadab.domain.friend.core.entity.FriendshipStatus;
import com.devkor.ifive.nadab.domain.friend.core.repository.FriendshipRepository;
import com.devkor.ifive.nadab.domain.user.core.dto.UserSearchDto;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.user.infra.ProfileImageUrlBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendshipQueryService {

    private static final int SEARCH_PAGE_SIZE = 30;

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final ProfileImageUrlBuilder profileImageUrlBuilder;

    public FriendListResponse getFriends(Long userId) {
        List<Friendship> friendships = friendshipRepository.findByUserIdAndStatusWithUsers(userId, FriendshipStatus.ACCEPTED);

        int totalCount = friendships.size();

        List<FriendResponse> friends = friendships.stream()
                .map(f -> {
                    Long friendId = f.getOtherUserId(userId);
                    User friend = friendId.equals(f.getUser1().getId()) ? f.getUser1() : f.getUser2();
                    String profileImageUrl = profileImageUrlBuilder.buildUserProfileUrl(friend);
                    return new FriendResponse(
                            f.getId(),
                            friend.getNickname(),
                            profileImageUrl,
                            friend.getDeletedAt() != null
                    );
                })
                .toList();

        return new FriendListResponse(totalCount, friends);
    }

    public PendingFriendListResponse getReceivedRequests(Long userId) {
        List<Friendship> friendships = friendshipRepository.findReceivedPendingRequests(userId);

        int totalCount = friendships.size();

        List<PendingFriendResponse> requests = friendships.stream()
                .map(f -> {
                    User requester = f.getRequester();
                    String profileImageUrl = profileImageUrlBuilder.buildUserProfileUrl(requester);
                    return new PendingFriendResponse(
                            f.getId(),
                            requester.getNickname(),
                            profileImageUrl
                    );
                })
                .toList();

        return new PendingFriendListResponse(totalCount, requests);
    }

    public SearchUserListResponse searchUsers(Long userId, String keyword, String cursor) {
        // 페이징 설정 (limit+1 조회하여 hasNext 판단)
        Pageable pageable = PageRequest.of(0, SEARCH_PAGE_SIZE + 1);

        // 와일드카드 이스케이핑 및 LIKE 패턴 준비
        String escaped = escapeWildcards(keyword);
        String fullKeyword = "%" + escaped + "%";      // 부분 매칭
        String prefixKeyword = escaped + "%";          // 시작 매칭

        // 1. 받은 친구 요청 조회 (첫 페이지에서만, DB에서 keyword로 필터링)
        List<Friendship> receivedRequests = (cursor == null)
                ? friendshipRepository.findReceivedPendingRequestsByKeyword(userId, fullKeyword)
                : List.of();

        // 2. excludeUserIds 추출
        List<Long> excludeUserIds = receivedRequests.stream()
                .map(f -> f.getRequester().getId())
                .toList();
        // 빈 리스트는 null로 변환
        if (excludeUserIds.isEmpty()) {
            excludeUserIds = null;
        }

        // 3. 유저 검색 + 친구 관계 정보 (받은 요청 유저 제외)
        List<UserSearchDto> searchResults = userRepository.searchUsersWithRelationship(
                userId, fullKeyword, keyword, prefixKeyword, cursor, excludeUserIds, pageable
        );

        // 4. pendingRequests 생성
        List<SearchUserResponse> pendingRequests = receivedRequests.stream()
                .map(f -> new SearchUserResponse(
                        f.getId(),
                        f.getRequester().getNickname(),
                        profileImageUrlBuilder.buildUserProfileUrl(f.getRequester()),
                        RelationshipStatus.REQUEST_RECEIVED
                ))
                .toList();

        // 5. hasNext 및 nextCursor 계산
        boolean hasNext = searchResults.size() > SEARCH_PAGE_SIZE;
        String nextCursor = null;
        if (hasNext) {
            // 마지막 항목 제거 (limit+1번째 항목)
            searchResults = searchResults.subList(0, SEARCH_PAGE_SIZE);
            // nextCursor는 마지막 항목의 닉네임
            nextCursor = searchResults.get(searchResults.size() - 1).nickname();
        }

        // 6. DTO 변환
        List<SearchUserResponse> results = searchResults.stream()
                .map(dto -> SearchUserResponse.from(dto, userId, profileImageUrlBuilder))
                .toList();

        return new SearchUserListResponse(pendingRequests, results, nextCursor, hasNext);
    }

    private String escapeWildcards(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return "";
        }
        return keyword
                .replace("\\", "\\\\")  // \ -> \\
                .replace("%", "\\%")    // % -> \%
                .replace("_", "\\_");   // _ -> \_
    }
}