package com.devkor.ifive.nadab.domain.dailyreport.application;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.FeedResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.FeedListResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.ShareStatusResponse;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.FeedDto;
import com.devkor.ifive.nadab.domain.friend.core.entity.Friendship;
import com.devkor.ifive.nadab.domain.friend.core.entity.FriendshipStatus;
import com.devkor.ifive.nadab.domain.friend.core.repository.FriendshipRepository;
import com.devkor.ifive.nadab.domain.moderation.application.SharingSuspensionService;
import com.devkor.ifive.nadab.domain.moderation.core.repository.ContentReportRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.DefaultProfileType;
import com.devkor.ifive.nadab.domain.user.infra.ProfileImageUrlBuilder;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedQueryService {

    private final FriendshipRepository friendshipRepository;
    private final DailyReportRepository dailyReportRepository;
    private final ProfileImageUrlBuilder profileImageUrlBuilder;
    private final ContentReportRepository contentReportRepository;
    private final SharingSuspensionService sharingSuspensionService;

    public FeedListResponse getFeeds(Long userId) {
        // 1. ACCEPTED 상태의 친구 관계 조회
        List<Friendship> friendships = friendshipRepository
                .findByUserIdAndStatusWithUsers(userId, FriendshipStatus.ACCEPTED);

        // 2. 친구 ID 리스트 추출
        List<Long> friendIds = friendships.stream()
                .map(f -> f.getOtherUserId(userId))
                .toList();

        // 3. 친구가 없으면 빈 리스트 반환
        if (friendIds.isEmpty()) {
            return new FeedListResponse(List.of());
        }

        // 4. 공유 활동 중지된 유저 제외
        Set<Long> suspendedUserIds = new HashSet<>(
                sharingSuspensionService.getSharingSuspendedUserIds(friendIds)
        );
        List<Long> activeFriendIds = friendIds.stream()
                .filter(id -> !suspendedUserIds.contains(id))
                .toList();

        // 5. 공유 가능한 친구가 없으면 빈 리스트 반환
        if (activeFriendIds.isEmpty()) {
            return new FeedListResponse(List.of());
        }

        // 6. 당일 공유된 피드 조회
        LocalDate today = TodayDateTimeProvider.getTodayDate();
        List<FeedDto> feedDtos = dailyReportRepository.findSharedFeedsByFriendIds(today, activeFriendIds);

        // 7. 내가 신고한 글 제외
        List<Long> dailyReportIds = feedDtos.stream()
                .map(FeedDto::dailyReportId)
                .toList();

        Set<Long> reportedIds = new HashSet<>(
                contentReportRepository.findReportedDailyReportIdsByReporter(userId, dailyReportIds)
        );

        // 8. 필터링 및 응답 DTO 변환
        List<FeedResponse> feeds = feedDtos.stream()
                .filter(dto -> !reportedIds.contains(dto.dailyReportId()))
                .map(dto -> {
                    String profileUrl = buildProfileUrl(dto.profileImageKey(), dto.defaultProfileType());

                    return new FeedResponse(
                            dto.dailyReportId(),
                            dto.nickname(),
                            profileUrl,
                            dto.interestCode() != null ? dto.interestCode().name() : null,
                            dto.questionText(),
                            dto.answerContent(),
                            dto.emotionCode() != null ? dto.emotionCode().name() : null
                    );
                })
                .toList();

        return new FeedListResponse(feeds);
    }

    public ShareStatusResponse getShareStatus(Long userId) {
        LocalDate today = TodayDateTimeProvider.getTodayDate();
        return dailyReportRepository.findByUserIdAndDate(userId, today)
                .map(report -> new ShareStatusResponse(report.getIsShared()))
                .orElse(new ShareStatusResponse(false));
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
