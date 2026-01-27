package com.devkor.ifive.nadab.domain.dailyreport.application;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.FeedResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.FeedListResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.ShareStatusResponse;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.FeedDto;
import com.devkor.ifive.nadab.domain.friend.core.entity.Friendship;
import com.devkor.ifive.nadab.domain.friend.core.entity.FriendshipStatus;
import com.devkor.ifive.nadab.domain.friend.core.repository.FriendshipRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.DefaultProfileType;
import com.devkor.ifive.nadab.domain.user.infra.ProfileImageUrlBuilder;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedQueryService {

    private final FriendshipRepository friendshipRepository;
    private final DailyReportRepository dailyReportRepository;
    private final ProfileImageUrlBuilder profileImageUrlBuilder;

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

        // 4. 당일 공유된 피드 조회
        LocalDate today = TodayDateTimeProvider.getTodayDate();
        List<FeedDto> feedDtos = dailyReportRepository
                .findSharedFeedsByFriendIds(today, friendIds);

        // 5. 응답 DTO 변환
        List<FeedResponse> feeds = feedDtos.stream()
                .map(dto -> {
                    String profileUrl = buildProfileUrl(dto.profileImageKey(), dto.defaultProfileType());

                    return new FeedResponse(
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
        // 1. 당일 DailyReport 조회
        LocalDate today = TodayDateTimeProvider.getTodayDate();
        DailyReport report = dailyReportRepository.findByUserIdAndDate(userId, today)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DAILY_REPORT_NOT_FOUND));

        return new ShareStatusResponse(report.getIsShared());
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
