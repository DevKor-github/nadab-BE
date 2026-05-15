package com.devkor.ifive.nadab.domain.comment.application;

import com.devkor.ifive.nadab.domain.comment.api.dto.response.CommentListResponse;
import com.devkor.ifive.nadab.domain.comment.api.dto.response.CommentResponse;
import com.devkor.ifive.nadab.domain.comment.core.dto.SubCommentCountDto;
import com.devkor.ifive.nadab.domain.comment.core.entity.Comment;
import com.devkor.ifive.nadab.domain.comment.core.repository.CommentRepository;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.domain.friend.core.repository.FriendshipRepository;
import com.devkor.ifive.nadab.domain.moderation.core.repository.UserBlockRepository;
import com.devkor.ifive.nadab.domain.user.infra.ProfileImageUrlBuilder;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.ForbiddenException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final CommentRepository commentRepository;
    private final DailyReportRepository dailyReportRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserBlockRepository userBlockRepository;
    private final ProfileImageUrlBuilder profileImageUrlBuilder;

    public CommentListResponse getComments(Long dailyReportId, Long currentUserId, Long cursor) {
        Long reportOwnerId = dailyReportRepository.findReportOwnerIdById(dailyReportId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DAILY_REPORT_NOT_FOUND));
        checkCommentViewAccess(dailyReportId, reportOwnerId, currentUserId);
        List<Long> excludedUserIds = getExcludedUserIds(currentUserId);

        List<Comment> comments = commentRepository.findTopLevelComments(
                dailyReportId, cursor, excludedUserIds, PageRequest.of(0, DEFAULT_PAGE_SIZE + 1));

        boolean hasNext = comments.size() > DEFAULT_PAGE_SIZE;
        if (hasNext) {
            comments = comments.subList(0, DEFAULT_PAGE_SIZE);
        }
        Long nextCursor = hasNext ? comments.get(comments.size() - 1).getId() : null;

        Map<Long, Long> subCountMap = buildSubCountMap(comments, excludedUserIds, currentUserId, reportOwnerId);

        List<CommentResponse> responses = comments.stream()
                .map(c -> {
                    boolean isMine = c.getAuthor().getId().equals(currentUserId);
                    boolean canViewContent = !c.isSecret() || isMine || currentUserId.equals(reportOwnerId);
                    boolean canDelete = isMine || currentUserId.equals(reportOwnerId);
                    return CommentResponse.from(
                            c.getId(),
                            canViewContent ? profileImageUrlBuilder.buildUserProfileUrl(c.getAuthor()) : null,
                            canViewContent ? c.getAuthor().getNickname() : null,
                            canViewContent ? c.getContent() : null,
                            c.getCreatedAt(),
                            canViewContent ? subCountMap.getOrDefault(c.getId(), 0L).intValue() : null,
                            c.isSecret(),
                            canViewContent,
                            isMine,
                            canDelete
                    );
                })
                .collect(Collectors.toList());

        return new CommentListResponse(responses, nextCursor, hasNext);
    }

    public CommentListResponse getSubComments(Long parentCommentId, Long currentUserId, Long cursor) {
        Comment parentComment = commentRepository.findByIdWithAuthorAndDailyReport(parentCommentId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));

        if (!parentComment.isTopLevel()) {
            throw new BadRequestException(ErrorCode.COMMENT_NOT_TOP_LEVEL);
        }

        Long dailyReportId = parentComment.getDailyReport().getId();
        Long reportOwnerId = dailyReportRepository.findReportOwnerIdById(dailyReportId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DAILY_REPORT_NOT_FOUND));
        checkCommentViewAccess(dailyReportId, reportOwnerId, currentUserId);

        if (parentComment.isSecret()) {
            boolean canViewParent = parentComment.getAuthor().getId().equals(currentUserId)
                    || reportOwnerId.equals(currentUserId);
            if (!canViewParent) {
                throw new ForbiddenException(ErrorCode.AUTH_ACCESS_DENIED);
            }
        }

        Long parentCommentAuthorId = parentComment.getAuthor().getId();
        List<Long> excludedUserIds = getExcludedUserIds(currentUserId);

        List<Comment> subComments = commentRepository.findSubComments(
                parentCommentId, cursor, excludedUserIds, PageRequest.of(0, DEFAULT_PAGE_SIZE + 1));

        boolean hasNext = subComments.size() > DEFAULT_PAGE_SIZE;
        if (hasNext) {
            subComments = subComments.subList(0, DEFAULT_PAGE_SIZE);
        }
        Long nextCursor = hasNext ? subComments.get(subComments.size() - 1).getId() : null;

        List<CommentResponse> responses = subComments.stream()
                .map(c -> {
                    boolean isMine = c.getAuthor().getId().equals(currentUserId);
                    boolean canViewContent = !c.isSecret()
                            || isMine
                            || currentUserId.equals(reportOwnerId)
                            || currentUserId.equals(parentCommentAuthorId);
                    boolean canDelete = isMine || currentUserId.equals(reportOwnerId);
                    return CommentResponse.from(
                            c.getId(),
                            canViewContent ? profileImageUrlBuilder.buildUserProfileUrl(c.getAuthor()) : null,
                            canViewContent ? c.getAuthor().getNickname() : null,
                            canViewContent ? c.getContent() : null,
                            c.getCreatedAt(),
                            null,
                            c.isSecret(),
                            canViewContent,
                            isMine,
                            canDelete
                    );
                })
                .collect(Collectors.toList());

        return new CommentListResponse(responses, nextCursor, hasNext);
    }

    private Map<Long, Long> buildSubCountMap(List<Comment> comments, List<Long> excludedUserIds,
                                              Long currentUserId, Long reportOwnerId) {
        if (comments.isEmpty()) {
            return Map.of();
        }
        List<Long> parentIds = comments.stream().map(Comment::getId).toList();

        // 현재 사용자가 비밀 대댓글을 열람할 수 있는 부모 댓글 ID 목록
        // - 리포트 소유자: 모든 부모 댓글의 비밀 대댓글 열람 가능
        // - 그 외: 자신이 작성한 부모 댓글의 비밀 대댓글만 열람 가능
        List<Long> visibleSecretParentIds;
        if (currentUserId.equals(reportOwnerId)) {
            visibleSecretParentIds = parentIds;
        } else {
            visibleSecretParentIds = comments.stream()
                    .filter(c -> c.getAuthor().getId().equals(currentUserId))
                    .map(Comment::getId)
                    .toList();
            if (visibleSecretParentIds.isEmpty()) {
                visibleSecretParentIds = List.of(-1L);
            }
        }

        return commentRepository.countVisibleSubCommentsByParentIds(
                        parentIds, excludedUserIds, currentUserId, visibleSecretParentIds)
                .stream()
                .collect(Collectors.toMap(SubCommentCountDto::parentCommentId, SubCommentCountDto::count));
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

    // TODO: 소셜 정지 중인 유저 ID도 포함
    private List<Long> getExcludedUserIds(Long userId) {
        List<Long> blocked = userBlockRepository.findBlockedUserIdsBidirectional(userId);
        // JPQL NOT IN 절에 빈 리스트 전달 방지
        return blocked.isEmpty() ? List.of(-1L) : blocked;
    }

}