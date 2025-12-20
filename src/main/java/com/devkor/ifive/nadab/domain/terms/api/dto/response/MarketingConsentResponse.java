package com.devkor.ifive.nadab.domain.terms.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "마케팅 동의 상태 응답")
public record MarketingConsentResponse(
        @Schema(description = "마케팅 동의 여부", example = "true")
        Boolean agreed
) {
}