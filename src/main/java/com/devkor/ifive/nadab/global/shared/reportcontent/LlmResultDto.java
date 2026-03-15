package com.devkor.ifive.nadab.global.shared.reportcontent;

public record LlmResultDto(
        String summary,
        StyledText discovered,
        StyledText improve
) {
}
