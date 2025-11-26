package com.devkor.ifive.nadab.domain.auth.api;

import com.devkor.ifive.nadab.domain.auth.api.dto.response.AuthorizationUrlResponse;
import com.devkor.ifive.nadab.domain.auth.api.dto.request.OAuth2LoginRequest;
import com.devkor.ifive.nadab.domain.auth.api.dto.response.TokenResponse;
import com.devkor.ifive.nadab.domain.auth.application.OAuth2Service;
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
 * - 공통 API (토큰 재발급, 로그아웃)
 */
@Tag(name = "인증 API", description = "OAuth2 로그인, 일반 로그인, 토큰 재발급, 로그아웃 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OAuth2Service oauth2Service;
    private final TokenService tokenService;
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
                            description = "잘못된 요청 - provider가 'naver' 또는 'google'이 아닌 경우",
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
        String authorizationUrl = oauth2Service.getAuthorizationUrl(oauth2Provider);

        return ApiResponseEntity.ok(new AuthorizationUrlResponse(authorizationUrl));
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
                    **signupStatus:**<br>
                    - PROFILE_INCOMPLETE: 프로필 입력 필요 (닉네임 미입력 상태, 신규 가입자)<br>
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
                                    - code 또는 state 값이 누락된 경우
                                    - provider가 'naver' 또는 'google'이 아닌 경우
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = """
                                    OAuth2 인증 실패
                                    - State 검증 실패 (CSRF 공격 방지, 10분 내 사용해야 함)
                                    - Authorization Code가 유효하지 않거나 만료된 경우
                                    - OAuth2 제공자로부터 Access Token 발급 실패
                                    - OAuth2 제공자로부터 사용자 정보 조회 실패
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = """
                                    이메일 중복
                                    - 해당 네이버, 구글 이메일이 다른 방법으로 이미 가입된 경우
                                    """,
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<TokenResponse>> oauth2Login(
            @Parameter(description = "OAuth2 제공자 (naver, google)", example = "naver")
            @PathVariable("provider") String provider,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "OAuth2 로그인 요청 (code, state)")
            @RequestBody @Valid OAuth2LoginRequest request,
            HttpServletResponse response
    ) {
        // Provider 검증 및 변환
        OAuth2Provider oauth2Provider = OAuth2Provider.fromString(provider);

        // Authorization Code로 토큰 발급 및 사용자 정보 조회
        TokenBundle tokenBundle = oauth2Service.executeOAuth2Login(oauth2Provider, request.code(), request.state());

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
                                    - 쿠키에 Refresh Token이 없는 경우
                                    - Refresh Token이 만료된 경우 (발급 후 14일 경과)
                                    - Refresh Token이 삭제된 경우 (로그아웃, 탈퇴, 또는 DB에 존재하지 않음)
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
                                    인증 실패
                                    - Authorization 헤더에 JWT Access Token이 없는 경우
                                    - JWT Access Token이 만료된 경우 (발급 후 1시간 경과)
                                    - JWT Access Token이 유효하지 않은 경우 (변조, 잘못된 서명)
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
}
