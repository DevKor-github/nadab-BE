package com.devkor.ifive.nadab.domain.dailyreport.core.dto;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;

public record PrepareDailyResultDto(
        AnswerEntry entry,
        Long reportId,
        Long userId
) {
}
