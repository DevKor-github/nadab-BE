package com.devkor.ifive.nadab.domain.question.application;

import com.devkor.ifive.nadab.domain.question.api.dto.response.DailyQuestionResponse;
import com.devkor.ifive.nadab.domain.question.application.helper.DailyQuestionSelector;
import com.devkor.ifive.nadab.domain.question.application.helper.QuestionLevelPolicy;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestionRevision;
import com.devkor.ifive.nadab.domain.question.core.entity.UserDailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.repository.UserDailyQuestionRepository;
import com.devkor.ifive.nadab.domain.question.core.service.DailyQuestionExposureService;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.AnswerEntryRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserInterestRepository;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionCommandService {

    private final UserRepository userRepository;
    private final UserDailyQuestionRepository userDailyQuestionRepository;
    private final UserInterestRepository userInterestRepository;
    private final AnswerEntryRepository answerEntryRepository;

    private final QuestionLevelPolicy questionLevelPolicy;
    private final DailyQuestionSelector dailyQuestionSelector;
    private final DailyQuestionExposureService dailyQuestionExposureService;

    public DailyQuestionResponse getOrCreateTodayQuestion(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        LocalDate today = TodayDateTimeProvider.getTodayDate();
        UserDailyQuestion udq = userDailyQuestionRepository.findByUserIdAndDate(userId, today)
                .orElseGet(() -> this.createTodayQuestion(userId, today));

        DailyQuestion question = udq.getDailyQuestion();
        DailyQuestionRevision revision = dailyQuestionExposureService
                .findLatestRevision(udq)
                .orElse(null);

        boolean answered = answerEntryRepository.existsActiveAnswer(userId, question.getId());

        return new DailyQuestionResponse(
                question.getId(),
                revision != null
                        ? revision.getInterest().getCode().toString()
                        : question.getInterest().getCode().toString(),
                revision != null ? revision.getQuestionText() : question.getQuestionText(),
                revision != null ? revision.getEmpathyGuide() : question.getEmpathyGuide(),
                revision != null ? revision.getHintGuide() : question.getHintGuide(),
                revision != null ? revision.getLeadingQuestionGuide() : question.getLeadingQuestionGuide(),
                answered,
                udq.isRerollUsed()
        );
    }

    /**
     * 오늘 첫 질문 생성:
     * - UserInterest의 interest_id 질문들 중 랜덤 1개
     * - 가입 2주 미만은 level = 1만
     * - 이미 답변한 질문은 제외
     */
    public UserDailyQuestion createTodayQuestion(Long userId, LocalDate todayKst) {
        // 동시성: 여러 요청이 동시에 들어오면 UNIQUE(user_id, date)로 한 번만 성공해야 함
        // -> insert 시도 후 unique 위반이면 다시 조회해서 반환
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        Long userInterestId = userInterestRepository.findInterestIdByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_INTEREST_NOT_FOUND));

        Integer levelOnly = questionLevelPolicy.levelOnlyFor(user, OffsetDateTime.now());

        DailyQuestion picked = dailyQuestionSelector.pickFirst(user.getId(), userInterestId, levelOnly);

        UserDailyQuestion saved;
        try {
            UserDailyQuestion udq = UserDailyQuestion.create(user, todayKst, picked);
            saved = userDailyQuestionRepository.save(udq);
        } catch (DataIntegrityViolationException e) {
            // 이미 생성됨(경합 상황)
            return userDailyQuestionRepository.findByUserIdAndDate(userId, todayKst)
                    .orElseThrow(() -> e);
        }

        dailyQuestionExposureService.recordInitialAssignment(saved);
        return saved;
    }

    /**
     * 리롤:
     * - reroll_used = false일 때만 허용
     * - 유저의 interest 내에서 랜덤 질문 (단, 현재 질문 제외)
     * - 가입 2주 미만이면 level = 1만
     * - 이미 답변한 질문은 제외
     */
    public DailyQuestionResponse rerollTodayQuestion(Long userId) {
        LocalDate today = TodayDateTimeProvider.getTodayDate();

        UserDailyQuestion udq = userDailyQuestionRepository.findByUserIdAndDate(userId, today)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DAILY_QUESTION_NOT_FOUND));

        if (udq.isRerollUsed()) {
            throw new ConflictException(ErrorCode.QUESTION_REROLL_LIMIT_EXCEEDED);
        }

        User user = udq.getUser();

        boolean alreadyAnswered = answerEntryRepository.existsByUserAndDate(user, today);
        if (alreadyAnswered) {
            throw new ConflictException(ErrorCode.QUESTION_ALREADY_ANSWERED);
        }

        Long userInterestId = userInterestRepository.findInterestIdByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_INTEREST_NOT_FOUND));

        Integer levelOnly = questionLevelPolicy.levelOnlyFor(user, OffsetDateTime.now());

        DailyQuestion newQ = dailyQuestionSelector.pickReroll(
                user.getId(),
                userInterestId,
                udq.getDailyQuestion().getId(),
                levelOnly
        );

        udq.rerollTo(newQ);
        dailyQuestionExposureService.recordReroll(udq, newQ);

        return new DailyQuestionResponse(
                newQ.getId(),
                newQ.getInterest().getCode().toString(),
                newQ.getQuestionText(),
                newQ.getEmpathyGuide(),
                newQ.getHintGuide(),
                newQ.getLeadingQuestionGuide(),
                false,
                true
        );
    }
}
