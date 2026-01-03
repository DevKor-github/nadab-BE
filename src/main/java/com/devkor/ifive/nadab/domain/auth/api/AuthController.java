package com.devkor.ifive.nadab.domain.auth.api;

import com.devkor.ifive.nadab.domain.auth.api.dto.request.LoginRequest;
import com.devkor.ifive.nadab.domain.auth.api.dto.request.ChangePasswordRequest;
import com.devkor.ifive.nadab.domain.auth.api.dto.request.ResetPasswordRequest;
import com.devkor.ifive.nadab.domain.auth.api.dto.request.RestoreRequest;
import com.devkor.ifive.nadab.domain.auth.api.dto.request.SignupRequest;
import com.devkor.ifive.nadab.domain.auth.api.dto.response.AuthorizationUrlResponse;
import com.devkor.ifive.nadab.domain.auth.api.dto.request.SocialLoginRequest;
import com.devkor.ifive.nadab.domain.auth.api.dto.response.TokenResponse;
import com.devkor.ifive.nadab.domain.auth.api.dto.response.WithdrawnInfoResponse;
import com.devkor.ifive.nadab.domain.auth.application.WithdrawalService;
import com.devkor.ifive.nadab.domain.auth.application.BasicAuthService;
import com.devkor.ifive.nadab.domain.auth.application.PasswordService;
import com.devkor.ifive.nadab.domain.auth.application.SocialAuthService;
import com.devkor.ifive.nadab.domain.auth.application.TokenService;
import com.devkor.ifive.nadab.domain.auth.application.TokenService.TokenBundle;
import com.devkor.ifive.nadab.domain.auth.infra.cookie.CookieManager;
import com.devkor.ifive.nadab.domain.auth.infra.oauth.OAuth2Provider;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import com.devkor.ifive.nadab.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 API 통합 컨트롤러
 * - OAuth2 로그인
 * - 일반 로그인
 * - 공통 API (토큰 재발급, 로그아웃, 비밀번호 변경, 회원 탈퇴, 회원 복구)
 */
@Tag(name = "인증 API", description = "OAuth2 로그인, 일반 로그인, 토큰 재발급, 로그아웃 API")
@RestController
@RequestMapping("${api_prefix}/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SocialAuthService socialAuthService;
    private final BasicAuthService basicAuthService;
    private final TokenService tokenService;
    private final PasswordService passwordService;
    private final WithdrawalService withdrawalService;
    private final CookieManager cookieManager;

    @GetMapping("/{provider}/url")
    @PermitAll
    @Operation(
            summary = "OAuth2 Authorization URL 조회",
            description = """
                    소셜 로그인 시작을 위한 Authorization URL을 생성합니다.<br>
                    반환된 URL로 사용자를 리다이렉트하면, 소셜 로그인 제공자의 인증 페이지로 이동합니다.<br>
                    인증 완료 후 redirect_uri로 code와 state가 전달되며, 이를 POST /{provider}/login에 전달하여 로그인을 완료합니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Authorization URL 조회 성공",
                            content = @Content(schema = @Schema(implementation = AuthorizationUrlResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "ErrorCode: AUTH_UNSUPPORTED_OAUTH2_PROVIDER - provider가 'naver' 또는 'google'이 아닌 경우",
                            content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<AuthorizationUrlResponse>> getAuthorizationUrl(
            @Parameter(description = "OAuth2 제공자 (naver, google)", example = "naver")
            @PathVariable("provider") String provider
    ) {
        // Provider 검증 및 변환
        OAuth2Provider oauth2Provider = OAuth2Provider.fromString(provider);

        // OAuth2 인증 URL 생성 (state 포함)
        String authorizationUrl = socialAuthService.getAuthorizationUrl(oauth2Provider);

        return ApiResponseEntity.ok(new AuthorizationUrlResponse(authorizationUrl));
    }

    @PostMapping("/signup")
    @PermitAll
    @Operation(
            summary = "일반 회원가입",
            description = """
                    이메일 인증 완료 후 회원가입을 진행합니다.<br>
                    일반 회원가입 시 약관 동의를 함께 처리합니다. 필수 약관(서비스 이용약관, 개인정보 처리방침, 만 14세 이상 확인)에 모두 동의해야 합니다.<br>
                    <br>
                    Access Token과 signupStatus는 응답 바디(JSON)로 반환되며, Refresh Token은 HttpOnly 쿠키로 자동 설정됩니다.<br>
                    회원가입 완료 후 signupStatus가 PROFILE_INCOMPLETE 상태이므로, 이 후 온보딩에서 프로필을 완성해야 합니다.<br>
                    <br>
                    **signupStatus:**<br>
                    - PROFILE_INCOMPLETE: 프로필 입력 필요
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "회원가입 성공",
                            content = @Content(schema = @Schema(implementation = TokenResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    잘못된 요청
                                    - ErrorCode: AUTH_EMAIL_NOT_VERIFIED - 이메일 인증 미완료
                                    - ErrorCode: TERMS_SERVICE_AGREEMENT_REQUIRED - 서비스 이용약관 미동의
                                    - ErrorCode: TERMS_PRIVACY_POLICY_REQUIRED - 개인정보 처리방침 미동의
                                    - ErrorCode: TERMS_AGE_VERIFICATION_REQUIRED - 만 14세 이상 확인 미동의
                                    - ErrorCode: VALIDATION_FAILED - 이메일/비밀번호 형식 오류
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "ErrorCode: EMAIL_ALREADY_EXISTS - 이미 사용 중인 이메일",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<TokenResponse>> signup(
            @RequestBody @Valid SignupRequest request,
            HttpServletResponse response
    ) {
        // 회원가입 및 토큰 발급
        TokenBundle tokenBundle = basicAuthService.signup(
                request.email(),
                request.password(),
                request.service(),
                request.privacy(),
                request.ageVerification(),
                request.marketing()
        );

        // Refresh Token을 HttpOnly 쿠키에 저장
        cookieManager.addRefreshTokenCookie(response, tokenBundle.refreshToken());

        return ApiResponseEntity.ok(
                new TokenResponse(tokenBundle.accessToken(), tokenBundle.signupStatus())
        );
    }

    @PostMapping("/login")
    @PermitAll
    @Operation(
            summary = "일반 로그인",
            description = """
                    이메일과 비밀번호로 로그인합니다.<br>
                    Access Token과 signupStatus는 응답 바디(JSON)로 반환되며, Refresh Token은 HttpOnly 쿠키로 자동 설정됩니다.<br>
                    <br>
                    **signupStatus:**<br>
                    - PROFILE_INCOMPLETE: 프로필 입력 필요 (온보딩 필요)<br>
                    - COMPLETED: 가입 완료 (모든 필수 정보 입력 완료)<br>
                    - WITHDRAWN: 회원 탈퇴 (14일 내 복구 가능)
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "로그인 성공",
                            content = @Content(schema = @Schema(implementation = TokenResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    - ErrorCode: AUTH_ACCOUNT_WITHDRAWN - 탈퇴한 계정
                                        - data 필드에 탈퇴 계정 정보 포함 (닉네임, 완전 삭제 예정일)
                                    - ErrorCode: VALIDATION_FAILED - 이메일 형식 오류
                                    """,
                            content = @Content(schema = @Schema(implementation = WithdrawnInfoResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "ErrorCode: AUTH_INVALID_PASSWORD - 비밀번호 불일치",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "ErrorCode: USER_NOT_FOUND - 등록되지 않은 이메일",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<TokenResponse>> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response
    ) {
        // 로그인 및 토큰 발급
        TokenBundle tokenBundle = basicAuthService.login(request.email(), request.password());

        // Refresh Token을 HttpOnly 쿠키에 저장
        cookieManager.addRefreshTokenCookie(response, tokenBundle.refreshToken());

        return ApiResponseEntity.ok(
                new TokenResponse(tokenBundle.accessToken(), tokenBundle.signupStatus())
        );
    }

    @PostMapping("/{provider}/login")
    @PermitAll
    @Operation(
            summary = "OAuth2 로그인",
            description = """
                    소셜 로그인 제공자로부터 받은 Authorization Code와 State를 사용하여 로그인을 완료합니다.<br>
                    Access Token과 signupStatus는 응답 바디(JSON)로 반환되며, Refresh Token은 HttpOnly 쿠키로 자동 설정됩니다.<br>
                    기존 회원은 바로 로그인 처리되며, 신규 사용자는 자동으로 회원가입 후 로그인됩니다.<br>
                    <br>
                    신규 가입자(signupStatus: PROFILE_INCOMPLETE)는 온보딩 과정에서 약관 동의(POST /terms/consent) 후 닉네임을 입력해야 합니다.<br>
                    <br>
                    **signupStatus:**<br>
                    - PROFILE_INCOMPLETE: 프로필 입력 필요 (신규 가입자, 약관 동의 + 닉네임 입력 필요)<br>
                    - COMPLETED: 가입 완료 (모든 필수 정보 입력 완료)<br>
                    - WITHDRAWN: 회원 탈퇴 (14일 내 복구 가능)
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "로그인 성공",
                            content = @Content(schema = @Schema(implementation = TokenResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    잘못된 요청
                                    - ErrorCode: AUTH_UNSUPPORTED_OAUTH2_PROVIDER - provider가 'naver' 또는 'google'이 아닌 경우
                                    - ErrorCode: VALIDATION_FAILED - code 또는 state 값이 누락된 경우
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = """
                                    OAuth2 인증 실패
                                    - ErrorCode: AUTH_INVALID_STATE - State 검증 실패 (CSRF 공격 방지, 10분 내 사용해야 함)
                                    - ErrorCode: AUTH_OAUTH2_TOKEN_FAILED - OAuth2 제공자로부터 Access Token 발급 실패
                                    - ErrorCode: AUTH_OAUTH2_USERINFO_FAILED - OAuth2 제공자로부터 사용자 정보 조회 실패
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "ErrorCode: AUTH_EMAIL_ALREADY_REGISTERED_WITH_DIFFERENT_METHOD - 해당 이메일이 다른 방법으로 이미 가입된 경우",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<TokenResponse>> oauth2Login(
            @Parameter(description = "OAuth2 제공자 (naver, google)", example = "naver")
            @PathVariable("provider") String provider,
            @RequestBody @Valid SocialLoginRequest request,
            HttpServletResponse response
    ) {
        // Provider 검증 및 변환
        OAuth2Provider oauth2Provider = OAuth2Provider.fromString(provider);

        // Authorization Code로 토큰 발급 및 사용자 정보 조회
        TokenBundle tokenBundle = socialAuthService.executeOAuth2Login(oauth2Provider, request.code(), request.state());

        // Refresh Token을 HttpOnly 쿠키에 저장
        cookieManager.addRefreshTokenCookie(response, tokenBundle.refreshToken());

        return ApiResponseEntity.ok(
                new TokenResponse(tokenBundle.accessToken(), tokenBundle.signupStatus())
        );
    }

    @PostMapping("/refresh")
    @PermitAll
    @Operation(
            summary = "토큰 재발급",
            description = """
                    Access Token이 만료되었을 때, HttpOnly 쿠키의 Refresh Token을 사용하여 새로운 Access Token과 signupStatus를 발급받습니다.<br>
                    보안을 위해 Refresh Token Rotation 방식을 사용하며, 기존 Refresh Token은 무효화되고 새로운 Refresh Token이 쿠키에 자동 설정됩니다.<br>
                    프론트엔드는 Access Token을 localStorage나 메모리에 저장하여 API 요청 시 Authorization 헤더에 포함해야 합니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "토큰 재발급 성공",
                            content = @Content(schema = @Schema(implementation = TokenResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = """
                                    인증 실패
                                    - ErrorCode: AUTH_REFRESH_TOKEN_NOT_FOUND - 쿠키에 Refresh Token이 없는 경우
                                    - ErrorCode: AUTH_INVALID_REFRESH_TOKEN - Refresh Token이 만료되었거나 DB에 존재하지 않음
                                    """,
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<TokenResponse>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 쿠키에서 Refresh Token 추출
        String refreshToken = cookieManager.extract(request);

        // 토큰 재발급 (Rotation)
        TokenBundle tokenBundle = tokenService.refreshTokens(refreshToken);

        // 새로운 Refresh Token을 쿠키에 저장
        cookieManager.addRefreshTokenCookie(response, tokenBundle.refreshToken());

        return ApiResponseEntity.ok(
                new TokenResponse(tokenBundle.accessToken(), tokenBundle.signupStatus())
        );
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "로그아웃",
            description = """
                    현재 로그인한 사용자의 모든 Refresh Token을 삭제하여 로그아웃합니다.<br>
                    DB에 저장된 Refresh Token과 브라우저 쿠키가 모두 제거되므로, 해당 사용자의 모든 기기에서 로그아웃 효과가 발생합니다.<br>
                    Access Token은 Bearer 형식으로 Authorization 헤더에 포함하여 요청해야 합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "로그아웃 성공"
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
    public ResponseEntity<ApiResponseDto<Void>> logout(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // DB에서 Refresh Token 삭제
        tokenService.revokeTokens(principal.getId());

        // 쿠키에서 Refresh Token 제거
        cookieManager.removeRefreshTokenCookie(response);

        return ApiResponseEntity.noContent();
    }

    @PostMapping("/password/reset")
    @PermitAll
    @Operation(
            summary = "비밀번호 찾기",
            description = """
                    이메일 인증 완료 후 비밀번호를 재설정합니다.<br>
                    - 이메일 인증(PASSWORD_RESET)을 먼저 완료해야 합니다
                    - 이전 비밀번호와 동일한 비밀번호는 사용할 수 없습니다
                    - 재설정 후 모든 기기에서 자동 로그아웃됩니다
                    """,
            responses = {
                    @ApiResponse(responseCode = "204", description = "비밀번호 재설정 성공"),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    잘못된 요청
                                    - ErrorCode: EMAIL_SOCIAL_ACCOUNT_PASSWORD_RESET_FORBIDDEN - 소셜 로그인 계정
                                    - ErrorCode: EMAIL_WITHDRAWN_ACCOUNT_PASSWORD_RESET_FORBIDDEN - 탈퇴한 계정
                                    - ErrorCode: AUTH_PASSWORD_REUSE_NOT_ALLOWED - 이전 비밀번호와 동일
                                    - ErrorCode: EMAIL_VERIFICATION_NOT_FOUND - 이메일 인증 미완료
                                    - ErrorCode: VALIDATION_FAILED - 이메일/비밀번호 형식 오류
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "404",
                            description = "ErrorCode: USER_NOT_FOUND - 등록되지 않은 이메일",
                            content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<Void>> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request
    ) {
        passwordService.resetPassword(request.email(), request.newPassword());
        return ApiResponseEntity.noContent();
    }

    @PatchMapping("/password")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "비밀번호 변경",
            description = """
                    로그인 상태(마이페이지)에서 비밀번호를 변경합니다.<br>
                    - 현재 비밀번호 확인이 필수입니다<br>
                    - 이전 비밀번호와 동일한 비밀번호는 사용할 수 없습니다<br>
                    - 변경 후 다른 기기에서는 자동 로그아웃됩니다<br>
                    - 현재 기기는 새로운 Access Token과 Refresh Token이 자동으로 발급되어 로그인 상태가 유지됩니다
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "비밀번호 변경 성공",
                            content = @Content(schema = @Schema(implementation = TokenResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    잘못된 요청
                                    - ErrorCode: AUTH_SOCIAL_ACCOUNT_PASSWORD_CHANGE_FORBIDDEN - 소셜 로그인 계정
                                    - ErrorCode: AUTH_PASSWORD_REUSE_NOT_ALLOWED - 이전 비밀번호와 동일
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "ErrorCode: AUTH_INVALID_PASSWORD - 현재 비밀번호 불일치",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<TokenResponse>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid ChangePasswordRequest request,
            HttpServletResponse response
    ) {
        // 비밀번호 변경 + 토큰 재발급
        TokenBundle tokenBundle = passwordService.changePassword(principal.getId(), request.currentPassword(), request.newPassword());

        // 새 Refresh Token을 HttpOnly 쿠키에 저장
        cookieManager.addRefreshTokenCookie(response, tokenBundle.refreshToken());

        return ApiResponseEntity.ok(
                new TokenResponse(tokenBundle.accessToken(), tokenBundle.signupStatus())
        );
    }

    @PostMapping("/withdrawal")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "회원 탈퇴",
            description = """
                    회원 탈퇴를 진행합니다.<br>
                    - 탈퇴 후 14일 동안 복구 가능합니다.<br>
                    - 모든 기기에서 자동 로그아웃됩니다.<br>
                    - 14일 후 자동으로 완전 삭제됩니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "탈퇴 성공"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "ErrorCode: AUTH_ALREADY_WITHDRAWN - 이미 탈퇴한 계정",
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
            HttpServletResponse response
    ) {
        // 회원 탈퇴
        withdrawalService.withdrawUser(principal.getId());

        // 쿠키에서 Refresh Token 제거
        cookieManager.removeRefreshTokenCookie(response);

        return ApiResponseEntity.noContent();
    }

    @PostMapping("/restore")
    @PermitAll
    @Operation(
            summary = "회원 복구 (일반 로그인)",
            description = """
                    탈퇴한 일반 계정을 복구합니다.<br>
                    - 이메일과 비밀번호로 본인 확인을 합니다.<br>
                    - 14일 이내에만 복구 가능합니다.<br>
                    - 복구 후 자동으로 로그인됩니다.<br>
                    <br>
                    **소셜 로그인 계정의 경우:**<br>
                    - 별도 복구 API가 필요 없습니다.<br>
                    - 소셜 로그인(POST /naver/login 또는 POST /google/login)을 시도하면 자동으로 복구됩니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "복구 성공 및 로그인",
                            content = @Content(schema = @Schema(implementation = TokenResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    잘못된 요청
                                    - ErrorCode: AUTH_NOT_WITHDRAWN - 탈퇴하지 않은 계정
                                    - ErrorCode: AUTH_SOCIAL_ACCOUNT_RESTORE_FORBIDDEN - 소셜 로그인 계정
                                    - ErrorCode: AUTH_RESTORE_PERIOD_EXPIRED - 복구 가능 기간(14일) 초과
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "ErrorCode: AUTH_INVALID_PASSWORD - 비밀번호 불일치",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "ErrorCode: USER_NOT_FOUND - 사용자를 찾을 수 없음",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<TokenResponse>> restoreBasicAccount(
            @RequestBody @Valid RestoreRequest request,
            HttpServletResponse response
    ) {
        // 계정 복구 및 토큰 발급
        TokenBundle tokenBundle = withdrawalService.restoreUser(request.email(), request.password());

        // Refresh Token을 HttpOnly 쿠키에 저장
        cookieManager.addRefreshTokenCookie(response, tokenBundle.refreshToken());

        return ApiResponseEntity.ok(
                new TokenResponse(tokenBundle.accessToken(), tokenBundle.signupStatus())
        );
    }

}