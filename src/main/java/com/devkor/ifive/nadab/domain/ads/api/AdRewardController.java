package com.devkor.ifive.nadab.domain.ads.api;

import com.devkor.ifive.nadab.domain.ads.api.dto.request.AdRewardSessionCreateRequest;
import com.devkor.ifive.nadab.domain.ads.api.dto.response.AdRewardQuoteResponse;
import com.devkor.ifive.nadab.domain.ads.api.dto.response.AdRewardSessionCreateResponse;
import com.devkor.ifive.nadab.domain.ads.api.dto.response.AdRewardSessionStatusResponse;
import com.devkor.ifive.nadab.domain.ads.application.AdRewardCallbackService;
import com.devkor.ifive.nadab.domain.ads.application.AdRewardSessionService;
import com.devkor.ifive.nadab.domain.ads.core.entity.AdRewardFeature;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import com.devkor.ifive.nadab.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "보상형 광고 크리스탈 충전 API", description = "크리스탈 부족 시 광고 시청으로 부족분을 충전(AdMob Rewarded + SSV)")
@RestController
@RequestMapping("${api_prefix}/ad-rewards")
@RequiredArgsConstructor
public class AdRewardController {

    private final AdRewardSessionService adRewardSessionService;
    private final AdRewardCallbackService adRewardCallbackService;

    @GetMapping("/quote")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "광고 보상 부족분 조회",
            description = """
                    크리스탈이 부족해 기능 실행(PDF 내보내기, 물어보기)이 막혔을 때 호출합니다. 광고로 채워야 할 부족분을 계산해 반환하는 순수 조회이며, 세션을 만들지 않습니다. </br>
                    선택한 기능의 비용(crystalCost)·현재 보유 크리스탈(balance)·부족분(requiredCrystal, 0 이상)을 반환합니다. </br>
                    크리스탈이 부족한 상황에서 호출하므로 보통 requiredCrystal > 0입니다. requiredCrystal이 0이면 이미 크리스탈이 충분하다는 의미(광고 시청 불필요)입니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "부족분 조회 성공",
                            content = @Content(schema = @Schema(implementation = AdRewardQuoteResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(
                            responseCode = "404",
                            description = "- ErrorCode: WALLET_NOT_FOUND - 지갑을 찾을 수 없음",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<AdRewardQuoteResponse>> quote(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam AdRewardFeature feature
    ) {
        return ApiResponseEntity.ok(adRewardSessionService.quote(principal.getId(), feature));
    }

    @PostMapping("/sessions")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "광고 보상 세션 발급",
            description = """
                    광고를 보기로 확정한 시점(광고 보기 버튼을 눌렀을 때)에 호출합니다. 부족분(requiredCrystal)을 확정 계산해 지급 예정 크리스탈(rewardAmount)로 세션을 만들고, AdMob custom_data에 담을 sessionKey를 반환합니다. </br>
                    흐름은 광고 보기 버튼 클릭 -> 이 api 호출(서버에서 세션 생성) -> 클라이언트에서 응답으로 받은 sessionKey를 AdMob custom_data에 담아 광고 시청입니다.</br>
                    ※ AdMob SSV에는 custom_data(sessionKey)만 설정하세요. user_id는 비워둡니다 — 내부 식별자를 넣으면 노출되며, 유저 바인딩은 sessionKey로 이뤄집니다. </br>
                    같은 사용자의 기존 대기(PENDING) 세션은 만료 처리되어, 활성 세션은 항상 1개만 유지됩니다. </br>
                    이미 크리스탈이 충분하면 AD_REWARD_NOT_NEEDED(400)로 거부되며, 이 경우 광고가 불필요합니다. </br>
                    광고 시청 후에는 지급 완료 여부를 세션 상태 조회(GET /ad-rewards/sessions/{sessionKey})로 확인하세요.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "세션 발급 성공",
                            content = @Content(schema = @Schema(implementation = AdRewardSessionCreateResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "- ErrorCode: AD_REWARD_NOT_NEEDED - 이미 크리스탈이 충분해 광고 시청이 필요하지 않음",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(
                            responseCode = "404",
                            description = """
                                    - ErrorCode: USER_NOT_FOUND - 사용자를 찾을 수 없음
                                    - ErrorCode: WALLET_NOT_FOUND - 지갑을 찾을 수 없음
                                    """,
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<AdRewardSessionCreateResponse>> createSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AdRewardSessionCreateRequest request
    ) {
        return ApiResponseEntity.ok(adRewardSessionService.createSession(principal.getId(), request.feature()));
    }

    @GetMapping("/sessions/{sessionKey}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "광고 보상 세션 상태 조회 (폴링)",
            description = """
                    광고 시청 후 서버의 크리스탈 지급 완료 여부를 sessionKey로 폴링합니다. 본인 세션만 조회할 수 있습니다. </br>
                    지급은 AdMob 클라이언트 콜백이 아니라 서버-서버 SSV 검증으로 이뤄지므로, 클라이언트 보상 콜백을 믿지 말고 이 API로 REWARDED를 확인한 뒤에 진행하세요. 광고 백엔드는 크리스탈 지급만 담당하고, 실제 차감·실행은 기존 기능 API가 그대로 처리합니다. </br>
                    광고 시청 완료 후 보통 1~2초 내에 REWARDED가 됩니다(서버-서버 SSV 전달 시간). 폴링 간격·타임아웃은 FE가 정하면 됩니다. </br>
                    REWARDED가 일정 시간 안 떠서 FE가 폴링을 멈추는 경우(= 폴링 타임아웃)의 사용자 안내(실패/재시도 표시/다시 기능화면으로 돌아가기 등)는 FE·기획 정책입니다. 다만 폴링을 멈춰도 리워드가 영구히 사라지는 건 아닙니다 — SSV가 늦게 도착하면 세션 유효시간 내에서 서버가 지급하기 때문입니다. </br>
                    status 값: </br>
                    - PENDING: 지급 대기 중(SSV 콜백 대기) </br>
                    - REWARDED: 지급 완료. rewardAmount만큼 충전되어 크리스탈이 충분해졌으므로, 크리스탈 부족으로 막혔던 그 기능의 기존 API(예: PDF 내보내기 생성 POST /pdf-exports, 물어보기 대화권 충전·사용)를 다시 호출하면 정상 차감·실행됩니다. </br>
                    - EXPIRED: 세션 TTL이 지나 만료됨(지급 없이 종료). 광고를 다시 보려면 세션 발급(POST /ad-rewards/sessions)부터 다시 시작하세요. </br>
                    status는 계산값으로, PENDING이라도 TTL이 지났으면 EXPIRED로 반환됩니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "상태 조회 성공",
                            content = @Content(schema = @Schema(implementation = AdRewardSessionStatusResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(
                            responseCode = "403",
                            description = "- ErrorCode: AD_REWARD_SESSION_ACCESS_FORBIDDEN - 본인의 광고 보상 세션이 아님",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "- ErrorCode: AD_REWARD_SESSION_NOT_FOUND - 광고 보상 세션을 찾을 수 없음",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<AdRewardSessionStatusResponse>> getSessionStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String sessionKey
    ) {
        return ApiResponseEntity.ok(adRewardSessionService.getStatus(principal.getId(), sessionKey));
    }

    // 공개 엔드포인트: 구글 SSV 콜백(인증 없음, 서명검증으로 보호). 구글은 HTTP 상태코드만 본다.
    @GetMapping("/ssv")
    @Operation(hidden = true)
    public ResponseEntity<Void> ssvCallback(HttpServletRequest request) {
        boolean retry = adRewardCallbackService.handle(request);
        // 일시 오류만 5xx로 구글 재시도 유도, 나머지 비즈니스 결과는 200
        return retry ? ResponseEntity.internalServerError().build() : ResponseEntity.ok().build();
    }
}