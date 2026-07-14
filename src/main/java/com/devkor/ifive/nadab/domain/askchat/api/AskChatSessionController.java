package com.devkor.ifive.nadab.domain.askchat.api;

import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatHomeResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatSessionResponse;
import com.devkor.ifive.nadab.domain.askchat.application.AskChatSessionService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Ask Chat API", description = "물어보기 채팅 세션 API")
@RestController
@RequestMapping("${api_prefix}/ask-chat")
@RequiredArgsConstructor
public class AskChatSessionController {

    private final AskChatSessionService askChatSessionService;

    @GetMapping("/home")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "물어보기 홈 진입",
            description = "사용자의 활성 물어보기 채팅 세션을 조회합니다. </br>" +
                    "활성 세션이 없으면 응답의 activeSession 필드를 null로 반환합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "물어보기 홈 진입 성공",
                            content = @Content(schema = @Schema(implementation = AskChatHomeResponse.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<AskChatHomeResponse>> enterHome(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        AskChatHomeResponse response = askChatSessionService.getHome(principal.getId());
        return ApiResponseEntity.ok(response);
    }

    @PostMapping("/sessions")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "물어보기 세션 시작",
            description = "사용자의 활성 물어보기 채팅 세션을 반환하고, 활성 세션이 없으면 새 세션을 생성합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "물어보기 세션 준비 성공",
                            content = @Content(schema = @Schema(implementation = AskChatSessionResponse.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<AskChatSessionResponse>> startSession(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        AskChatSessionResponse response = askChatSessionService.startSession(principal.getId());
        return ApiResponseEntity.ok(response);
    }
}
