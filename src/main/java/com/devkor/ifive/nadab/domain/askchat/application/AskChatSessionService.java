package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatHomeResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatSessionResponse;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSessionStatus;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatSessionRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AskChatSessionService {

    public static final int MAX_TURN_COUNT = 15;

    private final AskChatSessionRepository askChatSessionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public AskChatHomeResponse getHome(Long userId) {
        Optional<AskChatSession> activeSession = askChatSessionRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(
                userId,
                AskChatSessionStatus.ACTIVE
        );

        return AskChatHomeResponse.from(activeSession, MAX_TURN_COUNT);
    }

    @Transactional
    public AskChatSessionResponse startSession(Long userId) {
        AskChatSession session = askChatSessionRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(
                        userId,
                        AskChatSessionStatus.ACTIVE
                )
                .orElseGet(() -> createSession(userId));

        return AskChatSessionResponse.from(session, MAX_TURN_COUNT);
    }

    private AskChatSession createSession(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        return askChatSessionRepository.save(AskChatSession.start(user));
    }
}
