package com.devkor.ifive.nadab.domain.wallet.api;

import com.devkor.ifive.nadab.domain.wallet.api.dto.response.WalletBalanceResponse;
import com.devkor.ifive.nadab.domain.wallet.application.WalletQueryService;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import com.devkor.ifive.nadab.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "지갑 API", description = "지갑, 크리스탈 관련 API")
@RestController
@RequestMapping("${api_prefix}/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletQueryService walletQueryService;

    @GetMapping("/balance")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "크리스탈 조회",
            description = "사용자의 현재 크리스탈을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(schema = @Schema(implementation = WalletBalanceResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = """
                                    - ErrorCode: USER_NOT_FOUND: 사용자를 찾을 수 없음
                                    - ErrorCode: WALLET_NOT_FOUND: 사용자의 지갑을 찾을 수 없음
                                    """,
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<WalletBalanceResponse>> getWalletBalance(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        WalletBalanceResponse response = walletQueryService.getWalletBalance(userPrincipal.getId());
        return ApiResponseEntity.ok(response);
    }
}
