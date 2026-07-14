package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatMessageResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatQuestionSendResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatSessionResponse;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessage;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSessionStatus;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatMessageRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatSessionRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AskChatMessageCommandService {

    private final AskChatSessionRepository askChatSessionRepository;
    private final AskChatMessageRepository askChatMessageRepository;
    private final UserRepository userRepository;

    @Transactional
    public AskChatQuestionSendResponse sendQuestion(Long userId, String content) {
        AskChatSession session = getOrCreateActiveSession(userId);
        validateTurnLimit(session);

        AskChatMessage userMessage = askChatMessageRepository.save(
                AskChatMessage.createUserMessage(session, content.trim())
        );

        return new AskChatQuestionSendResponse(
                AskChatSessionResponse.from(session, AskChatSessionService.MAX_TURN_COUNT),
                AskChatMessageResponse.from(userMessage)
        );
    }

    private AskChatSession getOrCreateActiveSession(Long userId) {
        return askChatSessionRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(
                        userId,
                        AskChatSessionStatus.ACTIVE
                )
                .orElseGet(() -> createSession(userId));
    }

    private AskChatSession createSession(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        return askChatSessionRepository.save(AskChatSession.start(user));
    }

    private void validateTurnLimit(AskChatSession session) {
        if (session.getAnsweredTurnCount() >= AskChatSessionService.MAX_TURN_COUNT) {
            throw new ConflictException(ErrorCode.ASK_CHAT_TURN_LIMIT_EXCEEDED);
        }
    }
}
