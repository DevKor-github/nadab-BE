package com.devkor.ifive.nadab.domain.dailyreport.core.service;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.AnswerEntryRepository;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AnswerEntryService {

    private final AnswerEntryRepository answerEntryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AnswerEntry getOrCreateTodayAnswerEntry(User user, DailyQuestion dq, String answerText) {

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        OffsetDateTime startOfToday =
                today.atStartOfDay(ZoneId.of("Asia/Seoul"))
                        .toOffsetDateTime();

        OffsetDateTime startOfTomorrow =
                today.plusDays(1)
                        .atStartOfDay(ZoneId.of("Asia/Seoul"))
                        .toOffsetDateTime();

        return answerEntryRepository.findByUserAndCreatedAtBetween(user, startOfToday, startOfTomorrow)
                .orElseGet(() -> answerEntryRepository.save(AnswerEntry.create(user, dq, answerText)));
    }
}

