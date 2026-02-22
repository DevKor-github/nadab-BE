package com.devkor.ifive.nadab.domain.dailyreport.application;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.DailyReportResponse;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.AnswerEntryRepository;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyReportQueryService {

    private final UserRepository userRepository;
    private final DailyReportRepository dailyReportRepository;
    private final AnswerEntryRepository answerEntryRepository;

    public DailyReportResponse getDailyReport(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        LocalDate today = TodayDateTimeProvider.getTodayDate();

        AnswerEntry entry = answerEntryRepository.findByUserAndDate(user, today)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ANSWER_NOT_FOUND));

        DailyReport report = dailyReportRepository.findByAnswerEntryAndDate(entry, today)
                .orElseThrow(() -> new NotFoundException(ErrorCode.DAILY_REPORT_NOT_FOUND));

        return new DailyReportResponse(
                entry.getContent(),
                report.getContent(),
                report.getEmotion().getCode().toString(),
                report.getIsShared()
        );
    }
}
