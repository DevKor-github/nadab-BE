package com.devkor.ifive.nadab.domain.user.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유저 관심 주제 업데이트 요청")
public record UpdateUserInterestRequest(
        @Schema(
                description = "유저가 설정하고자 하는 새로운 관심 주제 코드입니다.",
                example = "RELATIONSHIP"
        )
        String interestCode
) {
}
