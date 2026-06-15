package com.devkor.ifive.nadab.domain.dailyreport.application;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.FeedResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.FeedListResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.ShareStatusResponse;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.FeedDto;
import com.devkor.ifive.nadab.domain.friend.core.entity.Friendship;
import com.devkor.ifive.nadab.domain.friend.core.entity.FriendshipStatus;
import com.devkor.ifive.nadab.domain.friend.core.repository.FriendshipRepository;
import com.devkor.ifive.nadab.domain.like.core.repository.DailyReportLikeRepository;
import com.devkor.ifive.nadab.domain.moderation.application.SharingSuspensionService;
import com.devkor.ifive.nadab.domain.moderation.core.repository.ContentReportRepository;
import com.devkor.ifive.nadab.domain.moderation.core.repository.UserBlockRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.DefaultProfileType;
import com.devkor.ifive.nadab.domain.user.infra.ProfileImageUrlBuilder;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedQueryService {

    private final FriendshipRepository friendshipRepository;
    private final DailyReportRepository dailyReportRepository;
    private final DailyReportLikeRepository dailyReportLikeRepository;
    private final ProfileImageUrlBuilder profileImageUrlBuilder;
    private final ContentReportRepository contentReportRepository;
    private final SharingSuspensionService sharingSuspensionService;
    private final UserBlockRepository userBlockRepository;

    public FeedListResponse getFeeds(Long userId) {
        LocalDate today = TodayDateTimeProvider.getTodayDate();

        // 1. 내 공유 리포트 조회
        Optional<FeedDto> myFeedDto = dailyReportRepository.findMySharedFeedByDate(userId, today);

        // 2. ACCEPTED 상태의 친구 관계 조회
        List<Friendship> friendships = friendshipRepository
                .findByUserIdAndStatusWithUsers(userId, FriendshipStatus.ACCEPTED);

        // 3. 친구 ID 리스트 추출
        List<Long> friendIds = friendships.stream()
                .map(f -> f.getOtherUserId(userId))
                .toList();

        // 4. 친구가 없으면 myReport만 매핑 후 반환
        if (friendIds.isEmpty()) {
            return toFeedListResponse(userId, myFeedDto, List.of());
        }

        // 5. 공유 활동 중지된 유저 제외
        Set<Long> suspendedUserIds = new HashSet<>(
                sharingSuspensionService.getSharingSuspendedUserIds(friendIds)
        );
        List<Long> activeFriendIds = friendIds.stream()
                .filter(id -> !suspendedUserIds.contains(id))
                .toList();

        // 6. 공유 가능한 친구가 없으면 myReport만 반환
        if (activeFriendIds.isEmpty()) {
            return toFeedListResponse(userId, myFeedDto, List.of());
        }

        // 7. 당일 공유된 피드 조회
        List<FeedDto> feedDtos = dailyReportRepository.findSharedFeedsByFriendIds(today, activeFriendIds);

        // 8. 내가 신고한 글 제외
        List<Long> friendReportIds = feedDtos.stream().map(FeedDto::dailyReportId).toList();
        Set<Long> reportedIds = friendReportIds.isEmpty() ? Set.of()
                : new HashSet<>(contentReportRepository.findReportedDailyReportIdsByReporter(userId, friendReportIds));
        List<FeedDto> filteredFeedDtos = feedDtos.stream()
                .filter(dto -> !reportedIds.contains(dto.dailyReportId()))
                .toList();

        return toFeedListResponse(userId, myFeedDto, filteredFeedDtos);
    }

    private FeedListResponse toFeedListResponse(Long userId, Optional<FeedDto> myFeedDto, List<FeedDto> feedDtos) {
        // 전체 reportId 수집 후 좋아요 정보 벌크 조회
        List<Long> allReportIds = Stream.concat(myFeedDto.stream(), feedDtos.stream())
                .map(FeedDto::dailyReportId)
                .toList();

        Set<Long> likedReportIds;
        Set<Long> reportIdsWithLikes;
        if (allReportIds.isEmpty()) {
            likedReportIds = Set.of();
            reportIdsWithLikes = Set.of();
        } else {
            likedReportIds = new HashSet<>(dailyReportLikeRepository.findLikedReportIds(allReportIds, userId));
            reportIdsWithLikes = new HashSet<>(
                    dailyReportLikeRepository.findReportIdsWithLikes(allReportIds, getExcludedUserIds(userId)));
        }

        FeedResponse myReport = myFeedDto
                .map(dto -> toFeedResponse(dto, likedReportIds, reportIdsWithLikes))
                .orElse(null);

        List<FeedResponse> feeds = feedDtos.stream()
                .map(dto -> toFeedResponse(dto, likedReportIds, reportIdsWithLikes))
                .toList();

        return new FeedListResponse(myReport, feeds);
    }

    public ShareStatusResponse getShareStatus(Long userId) {
        LocalDate today = TodayDateTimeProvider.getTodayDate();
        return dailyReportRepository.findByUserIdAndDate(userId, today)
                .map(report -> new ShareStatusResponse(report.getIsShared()))
                .orElse(new ShareStatusResponse(false));
    }

    private FeedResponse toFeedResponse(FeedDto dto, Set<Long> likedReportIds, Set<Long> reportIdsWithLikes) {
        String profileUrl = buildProfileUrl(dto.profileImageKey(), dto.defaultProfileType());
        String imageUrl = dto.imageKey() != null ? profileImageUrlBuilder.buildUrl(dto.imageKey()) : null;
        return new FeedResponse(
                dto.dailyReportId(),
                dto.nickname(),
                profileUrl,
                dto.interestCode() != null ? dto.interestCode().name() : null,
                dto.questionText(),
                dto.answerContent(),
                dto.emotionCode() != null ? dto.emotionCode().name() : null,
                imageUrl,
                likedReportIds.contains(dto.dailyReportId()),
                reportIdsWithLikes.contains(dto.dailyReportId())
        );
    }

    private List<Long> getExcludedUserIds(Long userId) {
        List<Long> blocked = userBlockRepository.findBlockedUserIdsBidirectional(userId);
        List<Long> suspended = sharingSuspensionService.getAllActiveSuspendedUserIds();

        Set<Long> combined = new HashSet<>(blocked);
        combined.addAll(suspended);
        combined.remove(userId);

        return combined.isEmpty() ? List.of(-1L) : new ArrayList<>(combined);
    }

    private String buildProfileUrl(String profileImageKey, DefaultProfileType defaultProfileType) {
        if (profileImageKey != null) {
            return profileImageUrlBuilder.buildUrl(profileImageKey);
        }
        if (defaultProfileType != null) {
            return profileImageUrlBuilder.buildDefaultUrl(defaultProfileType);
        }
        return null;
    }
}
