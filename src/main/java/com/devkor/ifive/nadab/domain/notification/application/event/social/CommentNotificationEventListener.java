package com.devkor.ifive.nadab.domain.notification.application.event.social;

import com.devkor.ifive.nadab.domain.comment.application.event.CommentCreatedEvent;
import com.devkor.ifive.nadab.domain.comment.application.event.SubCommentCreatedEvent;
import com.devkor.ifive.nadab.domain.comment.core.repository.CommentRepository;
import com.devkor.ifive.nadab.domain.notification.application.NotificationCommandService;
import com.devkor.ifive.nadab.domain.notification.core.entity.NotificationType;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.notification.message.NotificationContent;
import com.devkor.ifive.nadab.global.core.notification.message.NotificationMessageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentNotificationEventListener {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final NotificationMessageFactory messageFactory;
    private final NotificationCommandService notificationCommandService;

    @Async("notificationTaskExecutor")
    @EventListener
    public void handleCommentCreated(CommentCreatedEvent event) {
        try {
            if (event.getAuthorId().equals(event.getReportOwnerId())) {
                log.debug("Self-comment, skip notification: commentId={}", event.getCommentId());
                return;
            }

            User author = userRepository.findById(event.getAuthorId()).orElse(null);
            if (author == null || author.getDeletedAt() != null) {
                log.debug("Author not found or deleted, skip notification: authorId={}", event.getAuthorId());
                return;
            }

            User reportOwner = userRepository.findById(event.getReportOwnerId()).orElse(null);
            if (reportOwner == null || reportOwner.getDeletedAt() != null) {
                log.debug("Report owner not found or deleted, skip notification: reportOwnerId={}", event.getReportOwnerId());
                return;
            }

            Map<String, String> params = Map.of(
                    "senderName", author.getNickname(),
                    "commentContent", truncate(event.getContent())
            );
            NotificationContent content = messageFactory.createMessage(NotificationType.COMMENT_ON_MY_REPORT, params);

            String idempotencyKey = String.format("COMMENT_%d_REPORT_OWNER", event.getCommentId());
            notificationCommandService.sendNotification(
                    event.getReportOwnerId(),
                    NotificationType.COMMENT_ON_MY_REPORT,
                    content.title(),
                    content.body(),
                    content.inboxMessage(),
                    event.getDailyReportId().toString(),
                    idempotencyKey
            );
            log.debug("Comment notification sent: commentId={}, reportOwnerId={}", event.getCommentId(), event.getReportOwnerId());
        } catch (Exception e) {
            log.error("Failed to handle CommentCreatedEvent: commentId={}, error={}",
                    event.getCommentId(), e.getMessage(), e);
        }
    }

    @Async("notificationTaskExecutor")
    @EventListener
    public void handleSubCommentCreated(SubCommentCreatedEvent event) {
        try {
            User author = userRepository.findById(event.getAuthorId()).orElse(null);
            if (author == null || author.getDeletedAt() != null) {
                log.debug("Author not found or deleted, skip notification: authorId={}", event.getAuthorId());
                return;
            }

            Map<String, String> params = Map.of(
                    "senderName", author.getNickname(),
                    "commentContent", truncate(event.getContent())
            );

            // 1. 부모 댓글 작성자 알림 (author 제외, 역할 무관 최우선)
            if (!event.getAuthorId().equals(event.getParentCommentAuthorId())) {
                User parentCommentAuthor = userRepository.findById(event.getParentCommentAuthorId()).orElse(null);
                if (parentCommentAuthor == null || parentCommentAuthor.getDeletedAt() != null) {
                    log.debug("Parent comment author not found or deleted, skip notification: parentCommentAuthorId={}", event.getParentCommentAuthorId());
                } else {
                    NotificationContent replyContent = messageFactory.createMessage(
                            NotificationType.REPLY_ON_MY_COMMENT, params);
                    notificationCommandService.sendNotification(
                            event.getParentCommentAuthorId(),
                            NotificationType.REPLY_ON_MY_COMMENT,
                            replyContent.title(),
                            replyContent.body(),
                            replyContent.inboxMessage(),
                            event.getDailyReportId().toString(),
                            String.format("COMMENT_%d_PARENT_AUTHOR", event.getSubCommentId())
                    );
                    log.debug("Sub-comment notification sent to parent author: subCommentId={}, parentCommentAuthorId={}", event.getSubCommentId(), event.getParentCommentAuthorId());
                }
            }

            // 2. 참여자 알림 (author, 부모 댓글 작성자 제외) — 리포트 당사자도 참여자면 여기서 처리
            List<Long> excludeFromParticipants = List.of(
                    event.getAuthorId(),
                    event.getParentCommentAuthorId()
            );
            List<Long> participantIds = commentRepository.findDistinctSubCommentAuthorIds(
                    event.getParentCommentId(), excludeFromParticipants);

            // 3. 리포트 당사자가 참여자가 아닌 경우에만 COMMENT_ON_MY_REPORT
            boolean reportOwnerIsParentAuthor = event.getReportOwnerId().equals(event.getParentCommentAuthorId());
            boolean reportOwnerIsParticipant = participantIds.contains(event.getReportOwnerId());
            boolean reportOwnerIsAuthor = event.getReportOwnerId().equals(event.getAuthorId());

            if (!reportOwnerIsAuthor && !reportOwnerIsParentAuthor && !reportOwnerIsParticipant) {
                User reportOwner = userRepository.findById(event.getReportOwnerId()).orElse(null);
                if (reportOwner == null || reportOwner.getDeletedAt() != null) {
                    log.debug("Report owner not found or deleted, skip notification: reportOwnerId={}", event.getReportOwnerId());
                } else {
                    NotificationContent reportOwnerContent = messageFactory.createMessage(
                            NotificationType.COMMENT_ON_MY_REPORT, params);
                    notificationCommandService.sendNotification(
                            event.getReportOwnerId(),
                            NotificationType.COMMENT_ON_MY_REPORT,
                            reportOwnerContent.title(),
                            reportOwnerContent.body(),
                            reportOwnerContent.inboxMessage(),
                            event.getDailyReportId().toString(),
                            String.format("COMMENT_%d_REPORT_OWNER", event.getSubCommentId())
                    );
                    log.debug("Sub-comment notification sent to report owner: subCommentId={}, reportOwnerId={}", event.getSubCommentId(), event.getReportOwnerId());
                }
            }

            NotificationContent participantContent = messageFactory.createMessage(
                    NotificationType.REPLY_ON_PARTICIPATED_COMMENT, params);
            for (Long participantId : participantIds) {
                notificationCommandService.sendNotification(
                        participantId,
                        NotificationType.REPLY_ON_PARTICIPATED_COMMENT,
                        participantContent.title(),
                        participantContent.body(),
                        participantContent.inboxMessage(),
                        event.getDailyReportId().toString(),
                        String.format("COMMENT_%d_PARTICIPANT_%d", event.getSubCommentId(), participantId)
                );
            }
            log.debug("Sub-comment notifications sent: subCommentId={}, participantCount={}", event.getSubCommentId(), participantIds.size());
        } catch (Exception e) {
            log.error("Failed to handle SubCommentCreatedEvent: subCommentId={}, error={}",
                    event.getSubCommentId(), e.getMessage(), e);
        }
    }

    private String truncate(String content) {
        if (content == null) return "";
        return content.length() > 20 ? content.substring(0, 20) + "..." : content;
    }
}