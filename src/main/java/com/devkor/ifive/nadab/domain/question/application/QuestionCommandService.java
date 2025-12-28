package com.devkor.ifive.nadab.domain.question.application;

import com.devkor.ifive.nadab.domain.question.api.dto.response.DailyQuestionResponse;
import com.devkor.ifive.nadab.domain.question.application.helper.DailyQuestionSelector;
import com.devkor.ifive.nadab.domain.question.application.helper.QuestionLevelPolicy;
import com.devkor.ifive.nadab.domain.question.application.helper.WeightedInterestPicker;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.entity.UserDailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.repository.UserDailyQuestionRepository;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.AnswerEntryRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.InterestRepository;
import com.devkor.ifive.nadab.domain.user.core.repository.UserInterestRepository;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.ConflictException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionCommandService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final UserDailyQuestionRepository userDailyQuestionRepository;
    private final UserInterestRepository userInterestRepository;
    private final InterestRepository interestRepository;
    private final AnswerEntryRepository answerEntryRepository;

    private final QuestionLevelPolicy questionLevelPolicy;
    private final WeightedInterestPicker weightedInterestPicker;
    private final DailyQuestionSelector dailyQuestionSelector;

    public DailyQuestionResponse getOrCreateTodayQuestion(Long userId) {
        LocalDate today = LocalDate.now(KST);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. id: " + userId));

        UserDailyQuestion udq = userDailyQuestionRepository.findByUserIdAndDate(userId, today)
                .orElseGet(() -> this.createTodayQuestion(userId, today));

        DailyQuestion question = udq.getDailyQuestion();

        boolean answered = answerEntryRepository.existsActiveAnswer(userId, question.getId());

        return new DailyQuestionResponse(
                question.getId(),
                question.getInterest().getCode().toString(),
                question.getQuestionText(),
                question.getEmpathyGuide(),
                question.getHintGuide(),
                question.getLeadingQuestionGuide(),
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
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. id: " + userId));

            Long userInterestId = userInterestRepository.findInterestIdByUserId(userId)
                    .orElseThrow(() -> new NotFoundException("사용자의 관심 주제를 찾을 수 없습니다. id: " + userId));

            Integer levelOnly = questionLevelPolicy.levelOnlyFor(user, OffsetDateTime.now());

            DailyQuestion picked = dailyQuestionSelector.pickFirst(user.getId(), userInterestId, levelOnly);

            UserDailyQuestion udq = UserDailyQuestion.create(user, todayKst, picked);
            return userDailyQuestionRepository.save(udq);

        } catch (DataIntegrityViolationException e) {
            // 이미 생성됨(경합 상황)
            return userDailyQuestionRepository.findByUserIdAndDate(userId, todayKst)
                    .orElseThrow(() -> e);
        }
    }

    /**
     * 리롤:
     * - reroll_used = false일 때만 허용
     * - 관심사 가중치 랜덤: 내 interest 50%, 나머지 interest는 동등 분배
     * - 선택된 interest 내에서 랜덤 질문 (단, 현재 질문 제외)
     * - 가입 2주 미만이면 level = 1만
     * - 이미 답변한 질문은 제외
     */
    public DailyQuestionResponse rerollTodayQuestion(Long userId) {
        LocalDate today = LocalDate.now(KST);

        UserDailyQuestion udq = userDailyQuestionRepository.findByUserIdAndDate(userId, today)
                .orElseThrow(() -> new ConflictException("오늘의 첫 질문이 아직 생성되지 않았습니다."));

        if (udq.isRerollUsed()) {
            throw new BadRequestException("오늘의 질문은 하루에 한 번만 새로 받을 수 있습니다.");
        }

        User user = udq.getUser();

        Long userInterestId = userInterestRepository.findInterestIdByUserId(userId)
                .orElseThrow(() -> new NotFoundException("유저 관심 주제가 없습니다. id: " + userId));

        List<Long> allInterestIds = interestRepository.findAllIds();
        Long selectedInterestId = weightedInterestPicker.pickForReroll(userInterestId, allInterestIds);

        Integer levelOnly = questionLevelPolicy.levelOnlyFor(user, OffsetDateTime.now());

        DailyQuestion newQ = dailyQuestionSelector.pickReroll(
                user.getId(),
                selectedInterestId,
                udq.getDailyQuestion().getId(),
                levelOnly
        );

        udq.rerollTo(newQ);

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
