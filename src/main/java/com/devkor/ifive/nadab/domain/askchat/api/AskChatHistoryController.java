package com.devkor.ifive.nadab.domain.askchat.api;

import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatHistoryDetailResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatHistoryListResponse;
import com.devkor.ifive.nadab.domain.askchat.application.AskChatHistoryQueryService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "물어보기 API")
@RestController
@RequestMapping("${api_prefix}/ask-chat")
@RequiredArgsConstructor
public class AskChatHistoryController {

    private final AskChatHistoryQueryService askChatHistoryQueryService;

    @GetMapping("/histories")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "물어보기 히스토리 목록 조회",
            description = """
                    사용자의 물어보기 채팅 세션 목록을 최신순으로 조회합니다.  </br>
                    USER 메시지가 1개 이상 저장된 세션만 히스토리로 노출하며, 세션만 생성되고 질문이 없는 대화는 목록에 포함하지 않습니다.  </br>
                    page는 1부터 시작하며, size는 최대 50까지 요청할 수 있습니다.  </br>
                    응답은 히스토리 목록과 페이지 정보를 포함하며, 목록이 비어 있는지는 histories 배열 길이로 판단할 수 있습니다.  </br>
                    각 항목은 첫 질문 제목, 마지막 사용자 질문, 마지막 메시지 시각, createdDate를 카드 표시용으로 제공합니다.  </br>
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "히스토리 목록 조회 성공",
                            content = @Content(schema = @Schema(implementation = AskChatHistoryListResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "- ErrorCode: VALIDATION_FAILED - page는 1 이상, size는 1 이상 50 이하로 요청해야 함",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<AskChatHistoryListResponse>> getHistories(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        AskChatHistoryListResponse response = askChatHistoryQueryService.getHistories(
                principal.getId(),
                page,
                size
        );
        return ApiResponseEntity.ok(response);
    }

    @GetMapping("/histories/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "물어보기 히스토리 상세 조회",
            description = """
                    ask_history_02 화면에서 선택한 물어보기 채팅 세션의 전체 메시지를 시간순으로 조회합니다.  </br>
                    본인의 세션만 조회할 수 있으며, 다른 사용자의 세션이거나 존재하지 않는 세션이면 ASK_CHAT_SESSION_NOT_FOUND를 반환합니다.  </br>
                    과거 대화 상세 화면은 읽기 전용이므로 readOnly=true를 반환하며, 새 질문 입력 UI는 제공하지 않습니다.  </br>
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "히스토리 상세 조회 성공",
                            content = @Content(schema = @Schema(implementation = AskChatHistoryDetailResponse.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(
                            responseCode = "404",
                            description = "- ErrorCode: ASK_CHAT_SESSION_NOT_FOUND - 채팅 세션을 찾을 수 없음",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<AskChatHistoryDetailResponse>> getHistoryDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long sessionId
    ) {
        AskChatHistoryDetailResponse response = askChatHistoryQueryService.getHistoryDetail(
                principal.getId(),
                sessionId
        );
        return ApiResponseEntity.ok(response);
    }
}
