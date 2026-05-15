package com.devkor.ifive.nadab.domain.comment.application;

import com.devkor.ifive.nadab.domain.comment.application.event.CommentCreatedEvent;
import com.devkor.ifive.nadab.domain.comment.application.event.SubCommentCreatedEvent;
import com.devkor.ifive.nadab.domain.comment.core.entity.Comment;
import com.devkor.ifive.nadab.domain.comment.core.repository.CommentRepository;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.domain.friend.core.repository.FriendshipRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.exception.ForbiddenException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final DailyReportRepository dailyReportRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Long createComment(Long dailyReportId, Long authorId, String content, boolean isSecret) {
        // TODO: 소셜 정지 중이면 SOCIAL_SUSPENDED 에러 응답
        Long reportOwnerId = dailyReportRepository.findReportOwnerIdById(dailyReportId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DAILY_REPORT_NOT_FOUND));
        checkCommentWriteAccess(dailyReportId, reportOwnerId, authorId);

        DailyReport dailyReport = dailyReportRepository.getReferenceById(dailyReportId);
        User author = userRepository.getReferenceById(authorId);

        Comment comment = Comment.createTopLevel(dailyReport, author, content, isSecret);
        commentRepository.save(comment);

        eventPublisher.publishEvent(
                new CommentCreatedEvent(comment.getId(), dailyReportId, authorId, reportOwnerId, content));

        return comment.getId();
    }

    public Long createSubComment(Long parentCommentId, Long authorId, String content, boolean isSecret) {
        // TODO: 소셜 정지 중이면 SOCIAL_SUSPENDED 에러 응답
        Comment parentComment = commentRepository.findByIdWithAuthorAndDailyReport(parentCommentId)
                .orElseThrow(() -> new ConflictException(ErrorCode.COMMENT_DELETED));

        if (!parentComment.isTopLevel()) {
            throw new BadRequestException(ErrorCode.COMMENT_NOT_TOP_LEVEL);
        }

        // 비밀 댓글의 하위 대댓글은 강제 비밀 처리
        boolean finalIsSecret = parentComment.isSecret() || isSecret;

        Long dailyReportId = parentComment.getDailyReport().getId();
        Long reportOwnerId = dailyReportRepository.findReportOwnerIdById(dailyReportId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DAILY_REPORT_NOT_FOUND));
        checkCommentWriteAccess(dailyReportId, reportOwnerId, authorId);

        if (parentComment.isSecret()) {
            boolean canViewParent = parentComment.getAuthor().getId().equals(authorId)
                    || reportOwnerId.equals(authorId);
            if (!canViewParent) {
                throw new ForbiddenException(ErrorCode.AUTH_ACCESS_DENIED);
            }
        }

        User author = userRepository.getReferenceById(authorId);

        Comment subComment = Comment.createSubComment(author, parentComment, content, finalIsSecret);
        commentRepository.save(subComment);
        eventPublisher.publishEvent(new SubCommentCreatedEvent(
                subComment.getId(),
                dailyReportId,
                authorId,
                parentCommentId,
                parentComment.getAuthor().getId(),
                reportOwnerId,
                content
        ));

        return subComment.getId();
    }

    private void checkCommentWriteAccess(Long dailyReportId, Long reportOwnerId, Long currentUserId) {
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

    public void updateComment(Long commentId, Long userId, String content) {
        // TODO: 소셜 정지 중이면 SOCIAL_SUSPENDED 에러 응답
        Comment comment = commentRepository.findByIdWithAuthorAndDailyReport(commentId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.AUTH_ACCESS_DENIED);
        }

        comment.updateContent(content);
    }

    public void deleteComment(Long commentId, Long userId) {
        // TODO: 소셜 정지 중이면 SOCIAL_SUSPENDED 에러 응답
        Comment comment = commentRepository.findByIdWithAuthorAndDailyReport(commentId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));

        Long authorId = comment.getAuthor().getId();
        Long reportOwnerId = dailyReportRepository.findReportOwnerIdById(comment.getDailyReport().getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.DAILY_REPORT_NOT_FOUND));

        if (!userId.equals(authorId) && !userId.equals(reportOwnerId)) {
            throw new ForbiddenException(ErrorCode.AUTH_ACCESS_DENIED);
        }

        commentRepository.delete(comment);
    }
}