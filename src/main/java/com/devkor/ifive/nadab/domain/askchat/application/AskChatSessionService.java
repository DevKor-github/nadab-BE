package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatHomeResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatQuestionSendResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatSampleQuestionResponse;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSampleQuestion;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWallet;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatSampleQuestionRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatSessionRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatWalletRepository;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.AnswerEntryRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AskChatSessionService {

    public static final int MAX_TURN_COUNT = 15;
    public static final int MIN_ANSWER_COUNT_TO_START = 20;
    private static final int HOME_SAMPLE_QUESTION_SIZE = 3;

    private final AskChatSessionRepository askChatSessionRepository;
    private final AskChatWalletRepository askChatWalletRepository;
    private final AskChatSampleQuestionRepository askChatSampleQuestionRepository;
    private final UserWalletRepository userWalletRepository;
    private final UserRepository userRepository;
    private final AnswerEntryRepository answerEntryRepository;
    private final AskChatMessageCommandService askChatMessageCommandService;

    @Transactional(readOnly = true)
    public AskChatHomeResponse getHome(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        AskChatWallet askChatWallet = askChatWalletRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ASK_CHAT_WALLET_NOT_FOUND));
        UserWallet userWallet = userWalletRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND));

        return new AskChatHomeResponse(
                askChatWallet.getTotalTurnBalance(),
                user.getNickname(),
                userWallet.getCrystalBalance(),
                pickSampleQuestions()
        );
    }

    @Transactional
    public AskChatQuestionSendResponse startSession(Long userId, String content) {
        validateMinimumAnswerCount(userId);
        AskChatSession session = createSession(userId);
        return askChatMessageCommandService.sendQuestion(userId, session.getId(), content);
    }

    private void validateMinimumAnswerCount(Long userId) {
        if (answerEntryRepository.countByUserId(userId) < MIN_ANSWER_COUNT_TO_START) {
            throw new BadRequestException(ErrorCode.ASK_CHAT_NOT_ENOUGH_ANSWERS);
        }
    }

    private AskChatSession createSession(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        return askChatSessionRepository.save(AskChatSession.start(user));
    }

    private List<AskChatSampleQuestionResponse> pickSampleQuestions() {
        Map<InterestCode, List<AskChatSampleQuestion>> questionsByCategory = askChatSampleQuestionRepository
                .findByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .collect(Collectors.groupingBy(
                        AskChatSampleQuestion::getInterestCode,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<InterestCode> categories = new ArrayList<>(questionsByCategory.keySet());
        Collections.shuffle(categories);

        return categories.stream()
                .limit(HOME_SAMPLE_QUESTION_SIZE)
                .map(category -> pickOne(questionsByCategory.get(category)))
                .map(AskChatSampleQuestionResponse::from)
                .toList();
    }

    private AskChatSampleQuestion pickOne(List<AskChatSampleQuestion> sampleQuestions) {
        return sampleQuestions.get(ThreadLocalRandom.current().nextInt(sampleQuestions.size()));
    }
}
