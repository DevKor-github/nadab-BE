package com.devkor.ifive.nadab.domain.like.application;

import com.devkor.ifive.nadab.domain.comment.core.entity.Comment;
import com.devkor.ifive.nadab.domain.comment.core.repository.CommentRepository;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.domain.friend.core.entity.Friendship;
import com.devkor.ifive.nadab.domain.friend.core.entity.FriendshipStatus;
import com.devkor.ifive.nadab.domain.friend.core.repository.FriendshipRepository;
import com.devkor.ifive.nadab.domain.like.api.dto.response.LikeListResponse;
import com.devkor.ifive.nadab.domain.like.api.dto.response.LikerResponse;
import com.devkor.ifive.nadab.domain.like.core.repository.CommentLikeRepository;
import com.devkor.ifive.nadab.domain.like.core.repository.DailyReportLikeRepository;
import com.devkor.ifive.nadab.domain.moderation.core.repository.UserBlockRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.infra.ProfileImageUrlBuilder;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.exception.ForbiddenException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeQueryService {

    private final DailyReportLikeRepository dailyReportLikeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final DailyReportRepository dailyReportRepository;
    private final CommentRepository commentRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserBlockRepository userBlockRepository;
    private final ProfileImageUrlBuilder profileImageUrlBuilder;

    public LikeListResponse getReportLikers(Long dailyReportId, Long currentUserId) {
        Long reportOwnerId = dailyReportRepository.findReportOwnerIdById(dailyReportId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DAILY_REPORT_NOT_FOUND));

        if (!currentUserId.equals(reportOwnerId)) {
            throw new ForbiddenException(ErrorCode.DAILY_REPORT_LIKE_LIST_FORBIDDEN);
        }

        List<Long> excludedUserIds = getExcludedUserIds(currentUserId);
        List<User> likers = dailyReportLikeRepository.findLikersByReportId(dailyReportId, excludedUserIds);

        Set<Long> friendIds = getAcceptedFriendIds(currentUserId);
        List<LikerResponse> responses = likers.stream()
                .map(u -> new LikerResponse(
                        u.getId(),
                        profileImageUrlBuilder.buildUserProfileUrl(u),
                        u.getNickname(),
                        friendIds.contains(u.getId())
                ))
                .toList();

        return new LikeListResponse(responses);
    }

    public LikeListResponse getCommentLikers(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findByIdWithAuthorAndDailyReport(commentId)
                .orElseThrow(() -> commentRepository.existsById(commentId)
                        ? new ConflictException(ErrorCode.COMMENT_DELETED)
                        : new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));

        Long dailyReportId = comment.getDailyReport().getId();
        Long reportOwnerId = dailyReportRepository.findReportOwnerIdById(dailyReportId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DAILY_REPORT_NOT_FOUND));
        checkCommentViewAccess(dailyReportId, reportOwnerId, currentUserId);

        if (comment.isSecret()) {
            boolean isParentAuthor = !comment.isTopLevel() &&
                    commentRepository.findParentAuthorIdById(commentId)
                            .map(id -> id.equals(currentUserId))
                            .orElse(false);
            boolean canView = comment.getAuthor().getId().equals(currentUserId)
                    || reportOwnerId.equals(currentUserId)
                    || isParentAuthor;
            if (!canView) {
                throw new ForbiddenException(ErrorCode.AUTH_ACCESS_DENIED);
            }
        }

        List<Long> excludedUserIds = getExcludedUserIds(currentUserId);
        List<User> likers = commentLikeRepository.findLikersByCommentId(commentId, excludedUserIds);

        Set<Long> friendIds = getAcceptedFriendIds(currentUserId);
        List<LikerResponse> responses = likers.stream()
                .map(u -> new LikerResponse(
                        u.getId(),
                        profileImageUrlBuilder.buildUserProfileUrl(u),
                        u.getNickname(),
                        friendIds.contains(u.getId())
                ))
                .toList();

        return new LikeListResponse(responses);
    }

    private void checkCommentViewAccess(Long dailyReportId, Long reportOwnerId, Long currentUserId) {
        if (currentUserId.equals(reportOwnerId)) return;
        if (!dailyReportRepository.existsByIdAndIsSharedTrue(dailyReportId)) {
            throw new ForbiddenException(ErrorCode.AUTH_ACCESS_DENIED);
        }
        long smallerId = Math.min(currentUserId, reportOwnerId);
        long largerId = Math.max(currentUserId, reportOwnerId);
        if (!friendshipRepository.existsAcceptedByUserIds(smallerId, largerId)) {
            throw new ForbiddenException(ErrorCode.AUTH_ACCESS_DENIED);
        }
    }

    private Set<Long> getAcceptedFriendIds(Long userId) {
        return friendshipRepository.findByUserIdAndStatusWithUsers(userId, FriendshipStatus.ACCEPTED)
                .stream()
                .map(f -> f.getOtherUserId(userId))
                .collect(Collectors.toSet());
    }

    private List<Long> getExcludedUserIds(Long userId) {
        // TODO: 소셜 정지 중인 유저 ID도 포함
        List<Long> blocked = userBlockRepository.findBlockedUserIdsBidirectional(userId);
        return blocked.isEmpty() ? List.of(-1L) : blocked;
    }
}