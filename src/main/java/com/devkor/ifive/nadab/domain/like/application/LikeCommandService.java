package com.devkor.ifive.nadab.domain.like.application;

import com.devkor.ifive.nadab.domain.comment.core.entity.Comment;
import com.devkor.ifive.nadab.domain.comment.core.repository.CommentRepository;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.domain.friend.core.repository.FriendshipRepository;
import com.devkor.ifive.nadab.domain.like.core.entity.CommentLike;
import com.devkor.ifive.nadab.domain.like.core.entity.DailyReportLike;
import com.devkor.ifive.nadab.domain.like.core.repository.CommentLikeRepository;
import com.devkor.ifive.nadab.domain.like.core.repository.DailyReportLikeRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.exception.ForbiddenException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeCommandService {

    private final DailyReportLikeRepository dailyReportLikeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final DailyReportRepository dailyReportRepository;
    private final CommentRepository commentRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public void likeReport(Long dailyReportId, Long userId) {
        // TODO: 소셜 정지 중이면 SOCIAL_SUSPENDED 에러 응답
        Long reportOwnerId = dailyReportRepository.findReportOwnerIdById(dailyReportId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DAILY_REPORT_NOT_FOUND));

        if (userId.equals(reportOwnerId)) {
            throw new BadRequestException(ErrorCode.CANNOT_LIKE_OWN_CONTENT);
        }

        checkReportLikeAccess(dailyReportId, reportOwnerId, userId);

        if (dailyReportLikeRepository.existsByUserIdAndDailyReportId(userId, dailyReportId)) {
            return; // 이미 좋아요 — 멱등 처리
        }

        User user = userRepository.getReferenceById(userId);
        DailyReport dailyReport = dailyReportRepository.getReferenceById(dailyReportId);
        dailyReportLikeRepository.save(DailyReportLike.create(user, dailyReport));
    }

    public void unlikeReport(Long dailyReportId, Long userId) {
        DailyReportLike like = dailyReportLikeRepository.findByUserIdAndDailyReportId(userId, dailyReportId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.LIKE_NOT_FOUND));
        dailyReportLikeRepository.delete(like);
    }

    public void likeComment(Long commentId, Long userId) {
        // TODO: 소셜 정지 중이면 SOCIAL_SUSPENDED 에러 응답
        Comment comment = commentRepository.findByIdWithAuthorAndDailyReport(commentId)
                .orElseThrow(() -> commentRepository.existsById(commentId)
                        ? new ConflictException(ErrorCode.COMMENT_DELETED)
                        : new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));

        if (userId.equals(comment.getAuthor().getId())) {
            throw new BadRequestException(ErrorCode.CANNOT_LIKE_OWN_CONTENT);
        }

        Long dailyReportId = comment.getDailyReport().getId();
        Long reportOwnerId = dailyReportRepository.findReportOwnerIdById(dailyReportId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DAILY_REPORT_NOT_FOUND));
        checkCommentLikeAccess(dailyReportId, reportOwnerId, userId);

        if (comment.isSecret()) {
            boolean isParentAuthor = !comment.isTopLevel() &&
                    commentRepository.findParentAuthorIdById(commentId)
                            .map(id -> id.equals(userId))
                            .orElse(false);
            boolean canView = comment.getAuthor().getId().equals(userId)
                    || reportOwnerId.equals(userId)
                    || isParentAuthor;
            if (!canView) {
                throw new ForbiddenException(ErrorCode.AUTH_ACCESS_DENIED);
            }
        }

        if (commentLikeRepository.existsByUserIdAndCommentId(userId, commentId)) {
            return; // 이미 좋아요 — 멱등 처리
        }

        User user = userRepository.getReferenceById(userId);
        commentLikeRepository.save(CommentLike.create(user, comment));
    }

    public void unlikeComment(Long commentId, Long userId) {
        CommentLike like = commentLikeRepository.findByUserIdAndCommentId(userId, commentId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.LIKE_NOT_FOUND));
        commentLikeRepository.delete(like);
    }

    private void checkReportLikeAccess(Long dailyReportId, Long reportOwnerId, Long currentUserId) {
        if (!dailyReportRepository.existsByIdAndIsSharedTrue(dailyReportId)) {
            throw new ForbiddenException(ErrorCode.AUTH_ACCESS_DENIED);
        }
        long smallerId = Math.min(currentUserId, reportOwnerId);
        long largerId = Math.max(currentUserId, reportOwnerId);
        if (!friendshipRepository.existsAcceptedByUserIds(smallerId, largerId)) {
            throw new ForbiddenException(ErrorCode.AUTH_ACCESS_DENIED);
        }
    }

    private void checkCommentLikeAccess(Long dailyReportId, Long reportOwnerId, Long currentUserId) {
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
}