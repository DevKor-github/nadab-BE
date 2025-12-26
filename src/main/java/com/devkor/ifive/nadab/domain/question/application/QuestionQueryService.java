package com.devkor.ifive.nadab.domain.question.application;

import com.devkor.ifive.nadab.domain.question.core.entity.UserDailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.repository.UserDailyQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionQueryService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final UserDailyQuestionRepository userDailyQuestionRepository;
    private final QuestionCommandService questionCommandService;

    public UserDailyQuestion getOrCreateTodayQuestion(Long userId) {
        LocalDate today = LocalDate.now(KST);

        return userDailyQuestionRepository.findByUserIdAndDate(userId, today)
                .orElseGet(() -> questionCommandService.createTodayQuestion(userId, today));
    }
}
