package com.devkor.ifive.nadab.domain.dailyreport.core.service;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.AnswerEntryRepository;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
public class AnswerEntryService {

    private final AnswerEntryRepository answerEntryRepository;


    @Transactional
    public AnswerEntry getOrCreateTodayAnswerEntry(User user, DailyQuestion dq, String answerText, boolean isDayPassed) {

        LocalDate targetDate =
                isDayPassed ? TodayDateTimeProvider.getTodayDate().minusDays(1) : TodayDateTimeProvider.getTodayDate();

        return answerEntryRepository.findByUserAndDate(user, targetDate)
                .orElseGet(() -> {
                    try {
                        return answerEntryRepository.save(AnswerEntry.create(user, dq, answerText, targetDate));
                    } catch (DataIntegrityViolationException e) {
                        // 동시 요청에서 이미 누가 만들었을 수 있음 -> 재조회로 멱등 처리
                        return answerEntryRepository.findByUserAndDate(user, targetDate)
                                .orElseThrow(() -> e);
                    }
                });
    }
}

