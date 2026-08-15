package com.devkor.ifive.nadab.domain.askchat.api;

import com.devkor.ifive.nadab.domain.askchat.api.dto.request.AskChatQuestionRequest;
import com.devkor.ifive.nadab.domain.askchat.api.dto.request.AskChatSessionStartRequest;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatHomeResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatQuestionSendResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatRemainingMessageCountResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatTurnChargeResponse;
import com.devkor.ifive.nadab.domain.askchat.application.AskChatMessageCommandService;
import com.devkor.ifive.nadab.domain.askchat.application.AskChatSessionService;
import com.devkor.ifive.nadab.domain.askchat.application.AskChatWalletChargeService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "물어보기 API", description = "물어보기 채팅 API")
@RestController
@RequestMapping("${api_prefix}/ask-chat")
@RequiredArgsConstructor
public class AskChatSessionController {

    private final AskChatSessionService askChatSessionService;
    private final AskChatMessageCommandService askChatMessageCommandService;
    private final AskChatWalletChargeService askChatWalletChargeService;

    @GetMapping("/home")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "물어보기 홈 조회",
            description = """
                    홈 진입만으로 새 채팅 세션을 생성하지 않습니다. </br>
                    사용자의 누적 답변 개수가 20개 이상인 경우에만 물어보기 홈을 조회할 수 있습니다. </br>
                    응답에는 남은 메시지 횟수, 사용자 닉네임, 보유 크리스탈 수, 예시 질문 목록을 포함합니다. </br>
                    예시 질문은 사용자별로 10분 단위로 갱신되며 같은 시간 구간에는 동일하게 유지됩니다. </br>
                    히스토리 목록은 이 API에서 반환하지 않으며, 별도 히스토리 API를 사용해야 합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "물어보기 홈 조회 성공",
                            content = @Content(schema = @Schema(implementation = AskChatHomeResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "- ErrorCode: ASK_CHAT_NOT_ENOUGH_ANSWERS - 누적 답변 20개 미만",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(
                            responseCode = "404",
                            description = """
                                    - ErrorCode: USER_NOT_FOUND - 사용자를 찾을 수 없음
                                    - ErrorCode: WALLET_NOT_FOUND - 크리스탈 지갑을 찾을 수 없음
                                    - ErrorCode: ASK_CHAT_WALLET_NOT_FOUND - Ask Chat 대화권 지갑을 찾을 수 없음
                                    """,
                            content = @Content
                    )
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
            description = """
                    사용자가 새 채팅을 시작하면서 첫 질문을 함께 전송할 때 호출합니다. </br>
                    요청 본문의 content를 첫 USER 메시지로 저장하고, 기존 POST /ask-chat/messages와 동일한 답변 생성/실패 응답 구조를 반환합니다. </br>
                    세션 생성 후 첫 질문 처리 중 대화권 부족 등 예외가 발생하면 세션 생성도 함께 롤백됩니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "물어보기 세션 생성 및 첫 질문 전송 성공",
                            content = @Content(schema = @Schema(implementation = AskChatQuestionSendResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    - ErrorCode: VALIDATION_FAILED - 질문은 공백 제외 1자 이상 200자 이하로 요청해야 함
                                    - ErrorCode: ASK_CHAT_TURN_BALANCE_INSUFFICIENT - 사용 가능한 Ask Chat 대화권이 없음
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<AskChatQuestionSendResponse>> startSession(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AskChatSessionStartRequest request
    ) {
        AskChatQuestionSendResponse response = askChatSessionService.startSession(
                principal.getId(),
                request.content()
        );
        return ApiResponseEntity.ok(response);
    }

    @PostMapping("/turns/charge")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "물어보기 대화권 충전",
            description = """
                    보유 크리스탈 200개를 차감하고 물어보기 유료 대화권 10회를 충전합니다. </br>
                    잔여 크리스탈이 부족하면 대화권을 충전하지 않고 WALLET_INSUFFICIENT_BALANCE 에러 코드를 반환합니다. </br>
                    응답에는 충전 후 크리스탈 잔액과 무료/유료 대화권을 합산한 남은 메시지 횟수가 포함됩니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "대화권 충전 성공",
                            content = @Content(schema = @Schema(implementation = AskChatTurnChargeResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "- ErrorCode: WALLET_INSUFFICIENT_BALANCE - 보유 크리스탈 부족",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(
                            responseCode = "404",
                            description = """
                                    - ErrorCode: USER_NOT_FOUND - 사용자를 찾을 수 없음
                                    - ErrorCode: WALLET_NOT_FOUND - 크리스탈 지갑을 찾을 수 없음
                                    - ErrorCode: ASK_CHAT_WALLET_NOT_FOUND - Ask Chat 대화권 지갑을 찾을 수 없음
                                    """,
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<AskChatTurnChargeResponse>> chargeTurns(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        AskChatTurnChargeResponse response = askChatWalletChargeService.chargeTurns(principal.getId());
        return ApiResponseEntity.ok(response);
    }

    @GetMapping("/turns/remaining")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "물어보기 남은 메시지 횟수 조회",
            description = """
                    현재 사용자가 사용할 수 있는 물어보기 남은 메시지 횟수만 조회합니다. </br>
                    홈 전체 정보를 다시 조회하지 않고 질문 전송/충전 이후 카운터만 갱신할 때 사용할 수 있습니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "남은 메시지 횟수 조회 성공",
                            content = @Content(schema = @Schema(implementation = AskChatRemainingMessageCountResponse.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(
                            responseCode = "404",
                            description = "- ErrorCode: ASK_CHAT_WALLET_NOT_FOUND - Ask Chat 대화권 지갑을 찾을 수 없음",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<AskChatRemainingMessageCountResponse>> getRemainingTurns(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        AskChatRemainingMessageCountResponse response =
                askChatSessionService.getRemainingMessageCount(principal.getId());
        return ApiResponseEntity.ok(response);
    }

    @PostMapping("/messages")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "물어보기 질문 전송",
            description = """
                    사용자가 질문을 보낼 때 호출합니다. </br>
                    세션이 없거나 다른 사용자의 세션이면 ASK_CHAT_SESSION_NOT_FOUND를 반환하며, 질문 전송 시 새 세션을 자동 생성하지 않습니다. </br>
                    세션 생성은 POST /ask-chat/sessions에서 먼저 수행해야 합니다. </br>
                    질문 내용은 앞뒤 공백 제거 후 1자 이상 200자 이하만 허용합니다. </br>
                    사용 가능한 대화권이 모두 0회이면 메시지를 저장하지 않고 ASK_CHAT_TURN_BALANCE_INSUFFICIENT 에러 코드를 반환합니다. </br>
                    답변 생성이 성공한 경우에만 answeredTurnCount를 1 증가시키며, 15번째 성공 답변 후 해당 세션은 ENDED로 자동 전환됩니다. </br>
                    답변 생성 실패 시에는 응답의 assistantMessage는 null로 반환합니다. </br>
                    클라이언트에서는 answerGeneration.success를 기준으로 채팅 말풍선(성공 케이스) 또는 모달/토스트(실패 케이스)를 표시해야 합니다. </br>
                    실패 케이스에서는 answerGeneration.message를 사용합니다.
                    ENDED 세션 또는 answeredTurnCount가 15 이상인 세션에서는 메시지를 저장하지 않고 ASK_CHAT_TURN_LIMIT_EXCEEDED 에러 코드를 반환합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "질문 저장 성공",
                            content = @Content(schema = @Schema(implementation = AskChatQuestionSendResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    - ErrorCode: VALIDATION_FAILED - 질문은 공백 제외 1자 이상 200자 이하로 요청해야 함
                                    - ErrorCode: ASK_CHAT_TURN_BALANCE_INSUFFICIENT - 사용 가능한 Ask Chat 대화권이 없음
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(
                            responseCode = "404",
                            description = "- ErrorCode: ASK_CHAT_SESSION_NOT_FOUND - 채팅 세션을 찾을 수 없음",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "- ErrorCode: ASK_CHAT_TURN_LIMIT_EXCEEDED - 세션의 대화 횟수 제한 초과",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<AskChatQuestionSendResponse>> sendQuestion(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AskChatQuestionRequest request
    ) {
        AskChatQuestionSendResponse response = askChatMessageCommandService.sendQuestion(
                principal.getId(),
                request.sessionId(),
                request.content()
        );
        return ApiResponseEntity.ok(response);
    }
}
