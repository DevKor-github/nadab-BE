package com.devkor.ifive.nadab.domain.monthlyreport.core.service;

import com.devkor.ifive.nadab.domain.monthlyreport.application.helper.MonthlySocialSummaryCalculator;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlySocialSummaryContent;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlySocialInteractionCountDto;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlySocialQueryRepository;
import com.devkor.ifive.nadab.global.shared.util.dto.MonthRangeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonthlySocialSummaryService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final MonthlySocialQueryRepository monthlySocialQueryRepository;

    public MonthlySocialSummaryContent buildSocialSummary(Long userId, MonthRangeDto range) {
        OffsetDateTime startAt = range.monthStartDate().atStartOfDay(SEOUL).toOffsetDateTime();
        OffsetDateTime endAt = range.monthEndDate().plusDays(1).atStartOfDay(SEOUL).toOffsetDateTime();

        List<MonthlySocialInteractionCountDto> likeCounts =
                monthlySocialQueryRepository.countReceivedLikesByFriend(userId, startAt, endAt);
        List<MonthlySocialInteractionCountDto> commentCounts =
                monthlySocialQueryRepository.countReceivedCommentsByFriend(userId, startAt, endAt);

        return MonthlySocialSummaryCalculator.calculate(
                range.monthStartDate().getMonthValue(),
                likeCounts,
                commentCounts
        );
    }
}
