package com.devkor.ifive.nadab.domain.dailyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공유 시작 요청 응답")
public record ShareStartResponse(
        @Schema(
                description = """
                        - SHARED: 공유 성공
                        - SUSPENDED: 공유 차단됨 (신고 10건 이상 & 서로 다른 신고자 2명 이상)
                        """,
                example = "SUSPENDED"
        )
        ShareStartStatus status
) {
}