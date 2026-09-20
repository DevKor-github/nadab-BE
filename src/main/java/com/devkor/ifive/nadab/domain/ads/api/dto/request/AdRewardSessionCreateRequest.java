package com.devkor.ifive.nadab.domain.ads.api.dto.request;

import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardFeature;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record AdRewardSessionCreateRequest(
        @Schema(description = "광고 보상 대상 기능", example = "PDF_REPORT_AND_ANSWER")
        @NotNull
        AdRewardFeature feature
) {
}