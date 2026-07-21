package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatHistoryDetailResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatHistoryItemResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatHistoryListResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatMessageResponse;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessage;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageRole;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatMessageRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatSessionRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AskChatHistoryQueryService {

    private static final int MAX_PAGE_SIZE = 50;

    private final AskChatSessionRepository askChatSessionRepository;
    private final AskChatMessageRepository askChatMessageRepository;

    public AskChatHistoryListResponse getHistories(Long userId, int page, int size) {
        validatePageRequest(page, size);

        PageRequest pageRequest = PageRequest.of(page - 1, size);
        List<AskChatSession> sessions = askChatSessionRepository.findHistoriesByUserIdAndMessageRole(
                userId,
                AskChatMessageRole.USER,
                pageRequest
        );
        long totalCount = askChatSessionRepository.countHistoriesByUserIdAndMessageRole(
                userId,
                AskChatMessageRole.USER
        );
        int totalPages = totalCount == 0 ? 0 : (int) Math.ceil((double) totalCount / size);

        List<AskChatHistoryItemResponse> histories = sessions.stream()
                .map(this::toHistoryItem)
                .toList();

        return new AskChatHistoryListResponse(
                histories,
                histories.isEmpty(),
                totalCount,
                page,
                size,
                totalPages,
                page > 1 && totalPages > 0,
                totalPages > 0 && page < totalPages
        );
    }

    public AskChatHistoryDetailResponse getHistoryDetail(Long userId, Long sessionId) {
        AskChatSession session = askChatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ASK_CHAT_SESSION_NOT_FOUND));

        List<AskChatMessageResponse> messages = askChatMessageRepository.findAllBySessionIdOrderByCreatedAtAsc(sessionId)
                .stream()
                .map(AskChatMessageResponse::from)
                .toList();

        return new AskChatHistoryDetailResponse(
                session.getId(),
                session.getStatus(),
                session.getAnsweredTurnCount(),
                true,
                session.getCreatedAt(),
                session.getEndedAt(),
                messages
        );
    }

    private AskChatHistoryItemResponse toHistoryItem(AskChatSession session) {
        String title = askChatMessageRepository.findFirstBySessionIdAndRoleOrderByCreatedAtAsc(
                        session.getId(),
                        AskChatMessageRole.USER
                )
                .map(AskChatMessage::getContent)
                .orElse("");
        String lastUserQuestion = askChatMessageRepository.findFirstBySessionIdAndRoleOrderByCreatedAtDesc(
                        session.getId(),
                        AskChatMessageRole.USER
                )
                .map(AskChatMessage::getContent)
                .orElse(title);
        OffsetDateTime lastMessageAt = askChatMessageRepository.findFirstBySessionIdOrderByCreatedAtDesc(session.getId())
                .map(AskChatMessage::getCreatedAt)
                .orElse(session.getCreatedAt());

        return new AskChatHistoryItemResponse(
                session.getId(),
                title,
                lastUserQuestion,
                session.getCreatedAt().toLocalDate(),
                session.getStatus(),
                session.getAnsweredTurnCount(),
                lastMessageAt
        );
    }

    private void validatePageRequest(int page, int size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BadRequestException(ErrorCode.VALIDATION_FAILED);
        }
    }
}
