package com.devkor.ifive.nadab.domain.dailyreport.core.service;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.AnswerEntryRepository;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import com.devkor.ifive.nadab.global.shared.util.dto.TodayDateTimeRangeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
public class AnswerEntryService {

    private final AnswerEntryRepository answerEntryRepository;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AnswerEntry getOrCreateTodayAnswerEntry(User user, DailyQuestion dq, String answerText) {

        TodayDateTimeRangeDto range = TodayDateTimeProvider.getRange();

        LocalDate today = TodayDateTimeProvider.getTodayDate();

        return answerEntryRepository.findByUserAndCreatedAtBetween(user, range.startOfToday(), range.startOfTomorrow())
                .orElseGet(() -> answerEntryRepository.save(AnswerEntry.create(user, dq, answerText, today)));
    }
}

