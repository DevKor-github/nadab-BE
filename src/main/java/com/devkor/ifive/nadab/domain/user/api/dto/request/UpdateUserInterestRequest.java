package com.devkor.ifive.nadab.domain.user.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "유저 관심 주제 업데이트 요청")
public record UpdateUserInterestRequest(
        @Schema(
                description = "유저가 설정하고자 하는 새로운 관심 주제 코드입니다.",
                example = "RELATIONSHIP"
        )
        @NotBlank(message = "관심 주제 코드는 필수 입력 값입니다.")
        String interestCode
) {
}
