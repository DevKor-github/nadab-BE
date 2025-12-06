package com.devkor.ifive.nadab.domain.report.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record DailyReportRequest(
        @Schema(example = "당신이 다른 사람들에게 보여주는 모습과 진짜 당신 사이의 가장 큰 간극은 무엇인가요?")
        String question,

        @Schema(example = "겉으로는 침착하고 자신감 있어 보이지만, 내심에는 끊임없이 불안해해. 내가 하는 게 정말 옳은 건지, 더 잘할 수는 없을까 하는 의심이 계속 거기 있어.")
        String answer
) {
}
