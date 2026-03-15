package com.devkor.ifive.nadab.domain.terms.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "마케팅 동의 변경 요청")
public record UpdateMarketingConsentRequest(
        @Schema(description = "마케팅 정보 수신 동의 (true 또는 false)", example = "true")
        @NotNull(message = "마케팅 동의 여부는 필수입니다")
        Boolean agreed
) {
}