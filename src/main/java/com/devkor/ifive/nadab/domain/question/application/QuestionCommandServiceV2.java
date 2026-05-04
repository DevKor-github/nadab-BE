package com.devkor.ifive.nadab.domain.question.application;

import com.devkor.ifive.nadab.domain.dailyreport.core.repository.AnswerEntryRepository;
import com.devkor.ifive.nadab.domain.question.api.dto.response.DailyQuestionResponseV2;
import com.devkor.ifive.nadab.domain.question.application.helper.DailyQuestionSelector;
import com.devkor.ifive.nadab.domain.question.application.helper.QuestionLevelPolicy;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.entity.UserDailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.repository.UserDailyQuestionRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserInterestRepository;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionCommandServiceV2 {

    private final UserRepository userRepository;
    private final UserDailyQuestionRepository userDailyQuestionRepository;
    private final UserInterestRepository userInterestRepository;
    private final AnswerEntryRepository answerEntryRepository;

    private final QuestionLevelPolicy questionLevelPolicy;
    private final DailyQuestionSelector dailyQuestionSelector;

    public DailyQuestionResponseV2 getOrCreateTodayQuestion(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        LocalDate today = TodayDateTimeProvider.getTodayDate();
        UserDailyQuestion udq = userDailyQuestionRepository.findByUserIdAndDate(userId, today)
                .orElseGet(() -> this.createTodayQuestion(userId, today));

        DailyQuestion question = udq.getDailyQuestion();

        boolean answered = answerEntryRepository.existsActiveAnswer(userId, question.getId());

        return new DailyQuestionResponseV2(
                question.getId(),
                question.getInterest().getCode().toString(),
                question.getQuestionText(),
                question.getEmpathyGuide(),
                question.getHintGuide(),
                question.getLeadingQuestionGuide(),
                answered,
                udq.getRerollLeft()
        );
    }

    public UserDailyQuestion createTodayQuestion(Long userId, LocalDate todayKst) {
        // 동시성: 여러 요청이 동시에 들어오면 UNIQUE(user_id, date)로 한 번만 성공해야 함
        // -> insert 시도 후 unique 위반이면 다시 조회해서 반환
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

            Long userInterestId = userInterestRepository.findInterestIdByUserId(userId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.USER_INTEREST_NOT_FOUND));

            boolean isFirstQuestion = !(userDailyQuestionRepository.existsByUserId(userId));

            Integer levelOnly = questionLevelPolicy.levelOnlyForFirstTime(isFirstQuestion);

            DailyQuestion picked = dailyQuestionSelector.pickFirst(user.getId(), userInterestId, levelOnly);

            UserDailyQuestion udq = UserDailyQuestion.create(user, todayKst, picked);
            return userDailyQuestionRepository.save(udq);

        } catch (DataIntegrityViolationException e) {
            // 이미 생성됨(경합 상황)
            return userDailyQuestionRepository.findByUserIdAndDate(userId, todayKst)
                    .orElseThrow(() -> e);
        }
    }
}
