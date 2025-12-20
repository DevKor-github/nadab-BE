package com.devkor.ifive.nadab.domain.terms.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "약관 동의 요청")
public record TermsConsentRequest(
        @Schema(description = "서비스 이용약관 동의", example = "true")
        @NotNull(message = "서비스 이용약관 동의는 필수입니다")
        Boolean service,

        @Schema(description = "개인정보 처리방침 동의", example = "true")
        @NotNull(message = "개인정보 처리방침 동의는 필수입니다")
        Boolean privacy,

        @Schema(description = "만 14세 이상 확인", example = "true")
        @NotNull(message = "만 14세 이상 확인은 필수입니다")
        Boolean ageVerification,

        @Schema(description = "마케팅 정보 수신 동의 (true 또는 false)", example = "false")
        @NotNull(message = "마케팅 동의 여부는 필수입니다")
        Boolean marketing
) {
}