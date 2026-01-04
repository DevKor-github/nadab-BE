package com.devkor.ifive.nadab.domain.weeklyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * - 에러 응답
 * - 지닌 주에 작성된 오늘의 리포트 개수가 4개 미만인 경우 반환
 */
@Schema(description = "지난주에 작성된 오늘의 리포트 개수 응답")
public record CompletedCountResponse(
        long completedCount
) {
}
