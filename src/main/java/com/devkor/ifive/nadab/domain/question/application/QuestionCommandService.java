package com.devkor.ifive.nadab.domain.question.application;

import com.devkor.ifive.nadab.domain.question.application.helper.DailyQuestionSelector;
import com.devkor.ifive.nadab.domain.question.application.helper.QuestionLevelPolicy;
import com.devkor.ifive.nadab.domain.question.application.helper.WeightedInterestPicker;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.entity.UserDailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.repository.UserDailyQuestionRepository;
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

    private final QuestionLevelPolicy questionLevelPolicy;
    private final WeightedInterestPicker weightedInterestPicker;
    private final DailyQuestionSelector dailyQuestionSelector;

    /**
     * 오늘 첫 질문 생성:
     * - UserInterest의 interest_id 질문들 중 랜덤 1개
     * - 가입 2주 미만은 level=1만
     */
    public UserDailyQuestion createTodayQuestion(Long userId, LocalDate todayKst) {
        // 동시성: 여러 요청이 동시에 들어오면 UNIQUE(user_id, date)로 한 번만 성공해야 함
        // -> insert 시도 후 unique 위반이면 다시 조회해서 반환
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다. id: " + userId));

            Long userInterestId = userInterestRepository.findInterestIdByUserId(userId)
                    .orElseThrow(() -> new NotFoundException("유저 관심 주제가 없습니다. id: " + userId));

            Integer levelOnly = questionLevelPolicy.levelOnlyFor(user, OffsetDateTime.now());

            DailyQuestion picked = dailyQuestionSelector.pickFirst(userInterestId, levelOnly);

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
     */
    public UserDailyQuestion rerollTodayQuestion(Long userId) {
        LocalDate today = LocalDate.now(KST);

        UserDailyQuestion udq = userDailyQuestionRepository.findByUserIdAndDate(userId, today)
                .orElseThrow(() -> new ConflictException("오늘의 첫 질문이 아직 생성되지 않았습니다."));

        if (udq.isRerollUsed()) {
            throw new BadRequestException("오늘은 이미 리롤을 사용했습니다.");
        }

        User user = udq.getUser();

        Long userInterestId = userInterestRepository.findInterestIdByUserId(userId)
                .orElseThrow(() -> new NotFoundException("유저 관심 주제가 없습니다. id: " + userId));

        List<Long> allInterestIds = interestRepository.findAllIds();
        Long selectedInterestId = weightedInterestPicker.pickForReroll(userInterestId, allInterestIds);

        Integer levelOnly = questionLevelPolicy.levelOnlyFor(user, OffsetDateTime.now());

        DailyQuestion newQ = dailyQuestionSelector.pickReroll(
                selectedInterestId,
                udq.getDailyQuestion().getId(),
                levelOnly
        );

        udq.rerollTo(newQ);
        return udq;
    }
}
