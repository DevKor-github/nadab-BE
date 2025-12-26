package com.devkor.ifive.nadab.domain.question.application.helper;

import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.repository.DailyQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyQuestionSelector {

    private final DailyQuestionRepository dailyQuestionRepository;

    public DailyQuestion pickFirst(Long interestId, Integer levelOnly) {
        return dailyQuestionRepository.findRandomByInterest(interestId, levelOnly)
                .orElseThrow(() -> new IllegalStateException("조건에 맞는 질문이 없습니다."));
    }

    public DailyQuestion pickReroll(Long interestId, Long excludeQuestionId, Integer levelOnly) {
        return dailyQuestionRepository.findRandomByInterestExcluding(interestId, excludeQuestionId, levelOnly)
                .orElseThrow(() -> new IllegalStateException("리롤 가능한 질문이 없습니다."));
    }
}
