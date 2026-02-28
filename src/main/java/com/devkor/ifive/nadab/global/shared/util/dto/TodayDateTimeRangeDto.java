package com.devkor.ifive.nadab.global.shared.util.dto;

import java.time.OffsetDateTime;

public record TodayDateTimeRangeDto(
        OffsetDateTime startOfToday,
        OffsetDateTime startOfTomorrow
) {
}