package com.devkor.ifive.nadab.domain.moderation.application;

import com.devkor.ifive.nadab.domain.comment.core.entity.Comment;
import com.devkor.ifive.nadab.domain.comment.core.repository.CommentRepository;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.domain.friend.core.repository.FriendshipRepository;
import com.devkor.ifive.nadab.domain.moderation.core.entity.ContentReport;
import com.devkor.ifive.nadab.domain.moderation.core.entity.ReportReason;
import com.devkor.ifive.nadab.domain.moderation.core.repository.ContentReportRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.exception.ForbiddenException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ContentReportCommandService {

    private final ContentReportRepository contentReportRepository;
    private final DailyReportRepository dailyReportRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final SharingSuspensionService sharingSuspensionService;

    public void reportContent(Long reporterId, Long dailyReportId, Long commentId,
                              ReportReason reason, String customReason) {
        // 1. 입력값 검증
        validateCustomReason(reason, customReason);
        validateTarget(dailyReportId, commentId);

        // 2. 신고 생성
        User reporter = userRepository.getReferenceById(reporterId);
        ContentReport report = commentId != null
                ? buildCommentReport(reporter, reporterId, commentId, reason, customReason)
                : buildDailyReportReport(reporter, reporterId, dailyReportId, reason, customReason);

        // 3. 신고 저장 (동시성 예외 처리)
        try {
            contentReportRepository.save(report);
        } catch (DataIntegrityViolationException e) {
            boolean isDuplicate = commentId != null
                    ? contentReportRepository.existsByReporterIdAndCommentId(reporterId, commentId)
                    : contentReportRepository.existsByReporterIdAndDailyReportId(reporterId, dailyReportId);
            if (isDuplicate) {
                throw new ConflictException(ErrorCode.CONTENT_REPORT_ALREADY_EXISTS);
            }
            throw e;
        }

        // 4. 자동 정지 조건 체크
        sharingSuspensionService.checkAndTriggerSuspension(report.getReportedUser().getId());
    }

    private ContentReport buildDailyReportReport(User reporter, Long reporterId, Long dailyReportId,
                                                  ReportReason reason, String customReason) {
        // 중복 신고 검증
        if (contentReportRepository.existsByReporterIdAndDailyReportId(reporterId, dailyReportId)) {
            throw new ConflictException(ErrorCode.CONTENT_REPORT_ALREADY_EXISTS);
        }

        // DailyReport 조회
        DailyReport dailyReport = dailyReportRepository.findById(dailyReportId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DAILY_REPORT_NOT_FOUND));

        // 자기 신고 방지 검증
        User reportedUser = dailyReport.getAnswerEntry().getUser();
        if (reporterId.equals(reportedUser.getId())) {
            throw new BadRequestException(ErrorCode.CONTENT_REPORT_SELF_REPORT_FORBIDDEN);
        }

        // 접근 권한 검증 (오늘 공유된 친구 리포트만 신고 가능, 본인 글은 early return)
        checkReportAccess(dailyReportId, reportedUser.getId(), reporterId);

        return ContentReport.createForDailyReport(reporter, dailyReport, reportedUser, reason, customReason);
    }

    private ContentReport buildCommentReport(User reporter, Long reporterId, Long commentId,
                                              ReportReason reason, String customReason) {
        // 중복 신고 검증
        if (contentReportRepository.existsByReporterIdAndCommentId(reporterId, commentId)) {
            throw new ConflictException(ErrorCode.CONTENT_REPORT_ALREADY_EXISTS);
        }

        // 댓글 조회
        Comment comment = commentRepository.findByIdWithAuthorAndDailyReport(commentId)
                .orElseThrow(() -> commentRepository.existsById(commentId)
                        ? new ConflictException(ErrorCode.COMMENT_DELETED)
                        : new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));

        // 자기 신고 방지 검증
        User reportedUser = comment.getAuthor();
        if (reporterId.equals(reportedUser.getId())) {
            throw new BadRequestException(ErrorCode.CONTENT_REPORT_SELF_REPORT_FORBIDDEN);
        }

        // 게시글 접근 권한 검증 (오늘 공유된 친구 리포트만 신고 가능, 본인 글은 early return)
        Long dailyReportId = comment.getDailyReport().getId();
        Long reportOwnerId = dailyReportRepository.findReportOwnerIdById(dailyReportId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DAILY_REPORT_NOT_FOUND));
        checkReportAccess(dailyReportId, reportOwnerId, reporterId);

        // 비밀 댓글은 열람 권한자만 신고 가능 (작성자는 자기 신고 방지로 이미 차단됨)
        if (comment.isSecret()) {
            boolean isParentAuthor = !comment.isTopLevel() &&
                    commentRepository.findParentAuthorIdById(commentId)
                            .map(id -> id.equals(reporterId))
                            .orElse(false);
            boolean canView = reportOwnerId.equals(reporterId) || isParentAuthor;
            if (!canView) {
                throw new ForbiddenException(ErrorCode.AUTH_ACCESS_DENIED);
            }
        }

        return ContentReport.createForComment(reporter, comment, reportedUser, reason, customReason);
    }

    private void checkReportAccess(Long dailyReportId, Long reportOwnerId, Long reporterId) {
        if (reporterId.equals(reportOwnerId)) return;
        if (!dailyReportRepository.existsByIdAndIsSharedTrueAndDate(dailyReportId, TodayDateTimeProvider.getTodayDate())) {
            throw new ForbiddenException(ErrorCode.AUTH_ACCESS_DENIED);
        }
        long smallerId = Math.min(reporterId, reportOwnerId);
        long largerId = Math.max(reporterId, reportOwnerId);
        if (!friendshipRepository.existsAcceptedByUserIds(smallerId, largerId)) {
            throw new ForbiddenException(ErrorCode.AUTH_ACCESS_DENIED);
        }
    }

    private void validateTarget(Long dailyReportId, Long commentId) {
        if (dailyReportId == null && commentId == null) {
            throw new BadRequestException(ErrorCode.CONTENT_REPORT_INVALID);
        }
        if (dailyReportId != null && commentId != null) {
            throw new BadRequestException(ErrorCode.CONTENT_REPORT_INVALID);
        }
    }

    private void validateCustomReason(ReportReason reason, String customReason) {
        if (reason == ReportReason.OTHER) {
            if (customReason == null || customReason.isBlank()) {
                throw new BadRequestException(ErrorCode.CONTENT_REPORT_INVALID);
            }
            if (customReason.length() > 200) {
                throw new BadRequestException(ErrorCode.CONTENT_REPORT_INVALID);
            }
        }
    }
}