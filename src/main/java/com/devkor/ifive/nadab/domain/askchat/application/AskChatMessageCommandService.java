package com.devkor.ifive.nadab.domain.askchat.application;

import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatMessageResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatQuestionSendResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatSessionResponse;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerGenerationResult;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerPromptContext;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessage;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageReference;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatRagDocument;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSession;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSessionStatus;
import com.devkor.ifive.nadab.domain.askchat.core.properties.AskChatAnswerProperties;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatMessageReferenceRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatMessageRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatRagDocumentRepository;
import com.devkor.ifive.nadab.domain.askchat.core.repository.AskChatSessionRepository;
import com.devkor.ifive.nadab.domain.askchat.infra.AskChatAnswerLlmClient;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.exception.ai.AiServiceException;
import com.devkor.ifive.nadab.global.infra.llm.LlmTokenUsage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AskChatMessageCommandService {

    private static final int MAX_QUESTION_LENGTH = 200;
    private static final String ANSWER_GENERATION_FAILED_MESSAGE =
            "답변 생성에 오류가 발생했어요. 다시 시도해주세요.";

    private final AskChatSessionRepository askChatSessionRepository;
    private final AskChatMessageRepository askChatMessageRepository;
    private final AskChatMessageReferenceRepository askChatMessageReferenceRepository;
    private final AskChatRagDocumentRepository askChatRagDocumentRepository;
    private final UserRepository userRepository;
    private final AskChatAnswerContextService askChatAnswerContextService;
    private final AskChatAnswerLlmClient askChatAnswerLlmClient;
    private final AskChatAnswerProperties askChatAnswerProperties;

    @Transactional
    public AskChatQuestionSendResponse sendQuestion(Long userId, String content) {
        String normalizedContent = normalizeQuestionContent(content);
        AskChatSession session = getOrCreateActiveSession(userId);
        validateTurnLimit(session);

        AskChatAnswerPromptContext context = askChatAnswerContextService.build(
                userId,
                session.getId(),
                normalizedContent
        );

        AskChatMessage userMessage = askChatMessageRepository.save(
                AskChatMessage.createUserMessage(session, normalizedContent)
        );

        AskChatMessage assistantMessage;
        List<String> followUpQuestions;
        try {
            AskChatAnswerGenerationResult generationResult = askChatAnswerLlmClient.generate(context);
            assistantMessage = saveCompletedAssistantMessage(session, generationResult);
            saveMessageReferences(assistantMessage, generationResult);
            session.incrementAnsweredTurnCount();
            followUpQuestions = generationResult.answer().followUpQuestions();
        } catch (AiServiceException e) {
            assistantMessage = saveFailedAssistantMessage(session, e);
            followUpQuestions = List.of();
        }

        return new AskChatQuestionSendResponse(
                AskChatSessionResponse.from(session, AskChatSessionService.MAX_TURN_COUNT),
                AskChatMessageResponse.from(userMessage),
                AskChatMessageResponse.from(assistantMessage),
                followUpQuestions
        );
    }

    private AskChatMessage saveCompletedAssistantMessage(
            AskChatSession session,
            AskChatAnswerGenerationResult generationResult
    ) {
        LlmTokenUsage tokenUsage = generationResult.tokenUsage();
        return askChatMessageRepository.save(AskChatMessage.createAssistantMessage(
                session,
                generationResult.answer().answer(),
                generationResult.provider(),
                generationResult.model(),
                tokenUsage.inputTokens(),
                tokenUsage.outputTokens(),
                tokenUsage.totalTokens(),
                tokenUsage.thinkingTokens()
        ));
    }

    private AskChatMessage saveFailedAssistantMessage(AskChatSession session, AiServiceException exception) {
        return askChatMessageRepository.save(AskChatMessage.createFailedAssistantMessage(
                session,
                ANSWER_GENERATION_FAILED_MESSAGE,
                askChatAnswerProperties.getProvider(),
                askChatAnswerProperties.getModel(),
                exception.getErrorCode().getCode()
        ));
    }

    private void saveMessageReferences(
            AskChatMessage assistantMessage,
            AskChatAnswerGenerationResult generationResult
    ) {
        List<Long> referenceDocumentIds = generationResult.referenceDocumentIds();
        for (int i = 0; i < referenceDocumentIds.size(); i++) {
            AskChatRagDocument ragDocument = askChatRagDocumentRepository.getReferenceById(referenceDocumentIds.get(i));
            askChatMessageReferenceRepository.save(AskChatMessageReference.of(
                    assistantMessage,
                    ragDocument,
                    i + 1
            ));
        }
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

    private String normalizeQuestionContent(String content) {
        if (content == null) {
            throw new BadRequestException(ErrorCode.VALIDATION_FAILED);
        }

        String normalizedContent = content.trim();
        if (normalizedContent.isBlank() || normalizedContent.length() > MAX_QUESTION_LENGTH) {
            throw new BadRequestException(ErrorCode.VALIDATION_FAILED);
        }

        return normalizedContent;
    }
}
