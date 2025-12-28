package com.devkor.ifive.nadab.domain.dailyreport.application;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.DailyReportResponse;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.AnswerEntryRepository;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyReportQueryService {

    private final UserRepository userRepository;
    private final DailyReportRepository dailyReportRepository;
    private final AnswerEntryRepository answerEntryRepository;

    public DailyReportResponse getDailyReport(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. id: " + id));

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        OffsetDateTime startOfToday =
                today.atStartOfDay(ZoneId.of("Asia/Seoul"))
                        .toOffsetDateTime();

        OffsetDateTime startOfTomorrow =
                today.plusDays(1)
                        .atStartOfDay(ZoneId.of("Asia/Seoul"))
                        .toOffsetDateTime();

        AnswerEntry entry = answerEntryRepository.findByUserAndCreatedAtBetween(user, startOfToday, startOfTomorrow)
                .orElseThrow(() -> new NotFoundException("오늘의 답변 항목을 찾을 수 없습니다. userId: " + id));

        DailyReport report = dailyReportRepository.findByAnswerEntryAndCreatedAtBetween(entry, startOfToday, startOfTomorrow)
                .orElseThrow(() -> new NotFoundException("오늘의 리포트를 찾을 수 없습니다. userId: " + id));

        return new DailyReportResponse(
                report.getContent(),
                report.getEmotion().getCode().toString()
        );
    }
}
