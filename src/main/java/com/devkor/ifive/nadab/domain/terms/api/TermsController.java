package com.devkor.ifive.nadab.domain.terms.api;

import com.devkor.ifive.nadab.domain.terms.api.dto.request.TermsConsentRequest;
import com.devkor.ifive.nadab.domain.terms.api.dto.request.UpdateMarketingConsentRequest;
import com.devkor.ifive.nadab.domain.terms.api.dto.response.MarketingConsentResponse;
import com.devkor.ifive.nadab.domain.terms.api.dto.response.TermsCheckResponse;
import com.devkor.ifive.nadab.domain.terms.application.TermsCommandService;
import com.devkor.ifive.nadab.domain.terms.application.TermsQueryService;
import com.devkor.ifive.nadab.domain.terms.application.dto.TermsConsentInfo;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import com.devkor.ifive.nadab.global.security.principal.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "약관 API", description = "약관 관련 API")
@RestController
@RequestMapping("${api_prefix}")
@RequiredArgsConstructor
public class TermsController {

    private final TermsCommandService termsCommandService;
    private final TermsQueryService termsQueryService;

    @GetMapping("/terms/consent")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "약관 동의 상태 확인",
            description = """
                    현재 사용자가 모든 활성 약관에 동의했는지 확인합니다.

                    - requiresConsent가 true이면 재동의가 필요합니다.
                    - missingTerms에 재동의가 필요한 약관 타입이 반환됩니다 (필수/선택 약관 모두 포함).
                    - 홈화면 진입 시 호출하여 약관 업데이트 알림을 표시할 수 있습니다.
                    - 선택 약관(MARKETING)도 버전 업데이트 시 재동의가 필요하며, 사용자는 동의/거부를 선택할 수 있습니다.
                    
                    **응답 필드 사용 가이드:**
                    
                    1. **재동의 여부 확인 (홈화면)**
                    - requiresConsent: true이면 재동의 페이지로 이동
                    - missingTerms: 어떤 약관이 필요한지 확인
                    - service, privacy, ageVerification, marketing: 현재 약관 동의 상태
    
                    2. **재동의 페이지 구현**
                    - service, privacy, ageVerification, marketing: 현재 약관 동의 상태이므로 초기값으로 사용
                    - 사용자가 이미 동의한 약관은 그대로 표시하고 missingTerms에 포함된 새로 동의를 받아야할 약관만 다르게 표시하여 사용자에게 안내
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(schema = @Schema(implementation = TermsCheckResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<TermsCheckResponse>> checkTermsConsent(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        TermsConsentInfo info = termsQueryService.getTermsConsentInfo(principal.getId());

        return ApiResponseEntity.ok(
                new TermsCheckResponse(
                        info.requiresConsent(),
                        info.missingTerms(),
                        info.service(),
                        info.privacy(),
                        info.ageVerification(),
                        info.marketing()
                )
        );
    }

    @PostMapping("/terms/consent")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "약관 동의",
            description = """
                    약관에 동의합니다.

                    - 소셜 로그인 온보딩(PROFILE_INCOMPLETE 상태): 로그인 후 약관 동의 필수
                    - 약관 재동의: 약관 업데이트 시 재동의
                    - 일반 회원가입은 POST /auth/signup에서 약관 동의를 함께 처리하므로, 이 API를 사용하지 않습니다.

                    필수 약관(SERVICE, PRIVACY, AGE_VERIFICATION)에 모두 동의해야 합니다.
                    마케팅 약관(MARKETING)은 선택 사항입니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "성공",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "필수 약관 미동의",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> agreeToTerms(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody TermsConsentRequest request
    ) {
        termsCommandService.saveConsents(
                principal.getId(),
                request.service(),
                request.privacy(),
                request.ageVerification(),
                request.marketing()
        );
        return ApiResponseEntity.noContent();
    }

    @GetMapping("/terms/consent/marketing")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "마케팅 동의 상태 조회",
            description = "현재 사용자의 마케팅 수신 동의 여부를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(schema = @Schema(implementation = MarketingConsentResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<MarketingConsentResponse>> getMarketingConsent(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean agreed = termsQueryService.hasAgreedToMarketing(principal.getId());
        return ApiResponseEntity.ok(new MarketingConsentResponse(agreed));
    }

    @PatchMapping("/terms/consent/marketing")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "마케팅 동의 변경",
            description = "마케팅 수신 동의를 변경합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "성공",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> updateMarketingConsent(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateMarketingConsentRequest request
    ) {
        termsCommandService.updateMarketingConsent(principal.getId(), request.agreed());
        return ApiResponseEntity.noContent();
    }
}