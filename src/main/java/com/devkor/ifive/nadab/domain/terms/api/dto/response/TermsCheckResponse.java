package com.devkor.ifive.nadab.domain.terms.api.dto.response;

import com.devkor.ifive.nadab.domain.terms.core.entity.TermsType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "약관 동의 상태 확인 응답")
public record TermsCheckResponse(
        @Schema(description = "재동의 필요 여부", example = "false")
        Boolean requiresConsent,

        @Schema(description = "재동의가 필요한 약관 타입 목록")
        List<TermsType> missingTerms,

        @Schema(description = "현재 서비스 이용약관 동의 여부", example = "true")
        Boolean service,

        @Schema(description = "현재 개인정보 처리방침 동의 여부", example = "true")
        Boolean privacy,

        @Schema(description = "현재 만 14세 이상 확인 여부", example = "true")
        Boolean ageVerification,

        @Schema(description = "현재 마케팅 수신 동의 여부", example = "true")
        Boolean marketing
) {
}