package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatHomeResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatHistoryItemResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatSessionResponse;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessage;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageRole;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatMessageRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatSessionRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AskChatSessionService {

    public static final int MAX_TURN_COUNT = 15;
    private static final int HOME_RECENT_SESSION_SIZE = 20;

    private final AskChatSessionRepository askChatSessionRepository;
    private final AskChatMessageRepository askChatMessageRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public AskChatHomeResponse getHome(Long userId) {
        List<AskChatHistoryItemResponse> recentSessions = askChatSessionRepository.findHistoriesByUserIdAndMessageRole(
                        userId,
                        AskChatMessageRole.USER,
                        PageRequest.of(0, HOME_RECENT_SESSION_SIZE)
                )
                .stream()
                .map(this::toRecentSession)
                .toList();

        return AskChatHomeResponse.of(MAX_TURN_COUNT, recentSessions);
    }

    @Transactional
    public AskChatSessionResponse startSession(Long userId) {
        AskChatSession session = createSession(userId);
        return AskChatSessionResponse.from(session, MAX_TURN_COUNT);
    }

    private AskChatSession createSession(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        return askChatSessionRepository.save(AskChatSession.start(user));
    }

    private AskChatHistoryItemResponse toRecentSession(AskChatSession session) {
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
}
