package com.devkor.ifive.nadab.domain.auth.api;

import com.devkor.ifive.nadab.domain.auth.api.dto.request.WithdrawalRequestV2;
import com.devkor.ifive.nadab.domain.auth.application.AuthServiceV2;
import com.devkor.ifive.nadab.domain.auth.infra.cookie.CookieManager;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import com.devkor.ifive.nadab.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "인증 API V2", description = "인증 관련 API V2")
@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthControllerV2 {

    private final AuthServiceV2 authServiceV2;
    private final CookieManager cookieManager;

    @PostMapping("/withdrawal")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "회원 탈퇴 V2",
            description = """
                    회원 탈퇴를 진행합니다.
                    - 탈퇴 후 14일 동안 복구 가능합니다.<br>
                    - 모든 기기에서 자동 로그아웃됩니다.<br>
                    - 14일 후 자동으로 완전 삭제됩니다. <br>
                    - 탈퇴 사유를 함께 저장합니다. <br>
                    이때, reasons 필드에 OTHER가 포함된 경우 customReason 필드는 필수입니다. <br>
                    
                    **<reasons 필드 enum>** <br>
                    DAILY_LOGGING_BURDEN,           // 매일 기록이 부담 <br>
                    INSUFFICIENT_QUESTION_ANALYSIS, // 질문·분석 부족 <br>
                    LOSS_OF_INTEREST_IN_WRITING,    // 글쓰기 흥미 상실 <br>
                    PRIVACY_RECORD_CONCERN,         // 감정·기록 보안 우려 <br>
                    APP_ERROR_OR_SLOWNESS,          // 오류·속도 문제 <br>
                    OTHER                           // 기타(직접 입력) <br>
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "탈퇴 성공"),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    - ErrorCode: AUTH_WITHDRAWAL_REASON_REQUIRED - 사유 미선택
                                    - ErrorCode: AUTH_WITHDRAWAL_REASON_DUPLICATED - 사유 중복 선택
                                    - ErrorCode: AUTH_WITHDRAWAL_OTHER_REASON_REQUIRED - OTHER 선택 후 기타 사유 미입력
                                    - ErrorCode: AUTH_WITHDRAWAL_OTHER_REASON_TOO_LONG - 기타 사유 200자 초과
                                    - ErrorCode: AUTH_WITHDRAWAL_OTHER_REASON_NOT_ALLOWED - OTHER 미선택인데 기타 사유 입력
                                    - ErrorCode: AUTH_ALREADY_WITHDRAWN - 이미 탈퇴된 계정
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = """
                                    인증 실패 (JWT 토큰 관련)
                                    - ErrorCode: AUTH_TOKEN_EXPIRED - JWT Access Token 만료
                                    - ErrorCode: AUTH_TOKEN_SIGNATURE_INVALID - 토큰 서명 검증 실패
                                    - ErrorCode: AUTH_TOKEN_MALFORMED - 토큰 형식 오류
                                    - ErrorCode: AUTH_TOKEN_VERIFICATION_FAILED - 토큰 검증 실패
                                    - ErrorCode: AUTH_TOKEN_USERID_INVALID - 토큰의 유저 ID 형식 오류
                                    - ErrorCode: AUTH_TOKEN_ROLES_MISSING - 토큰에 권한 정보 없음
                                    """,
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> withdrawUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid WithdrawalRequestV2 request,
            HttpServletResponse response
    ) {
        authServiceV2.withdrawUser(principal.getId(), request.reasons(), request.customReason());
        cookieManager.removeRefreshTokenCookie(response);
        return ApiResponseEntity.noContent();
    }
}
