package com.devkor.ifive.nadab.domain.askchat.api;

import com.devkor.ifive.nadab.domain.askchat.api.dto.request.AskChatQuestionRequest;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatHomeResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatQuestionSendResponse;
import com.devkor.ifive.nadab.domain.askchat.api.dto.response.AskChatSessionResponse;
import com.devkor.ifive.nadab.domain.askchat.application.AskChatMessageCommandService;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "물어보기 API", description = "물어보기 채팅 세션 API")
@RestController
@RequestMapping("${api_prefix}/ask-chat")
@RequiredArgsConstructor
public class AskChatSessionController {

    private final AskChatSessionService askChatSessionService;
    private final AskChatMessageCommandService askChatMessageCommandService;

    @GetMapping("/home")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "물어보기 홈 진입",
            description = """
                    ask_home_01 화면 진입 시 호출합니다. </br>
                    홈 진입만으로 새 채팅 세션을 생성하지 않습니다. </br>
                    이 API는 새 세션 생성 전에 필요한 기본 정책 값만 반환하며, 세션 생성은 POST /ask-chat/sessions에서 명시적으로 수행합니다. </br>
                    크리스탈 및 대화권 잔여 횟수는 다음 브랜치에서 별도 정책으로 연결합니다.
                    """,
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
            description = """
                    사용자가 새 채팅을 시작할 때 호출합니다. </br>
                    이미 ACTIVE 세션이 있어도 기존 세션을 재사용하거나 종료하지 않고 매번 새 ACTIVE 세션을 생성합니다. </br>
                    이 API는 질문 메시지를 저장하지 않으며, 실제 질문 저장은 POST /ask-chat/messages에서 수행합니다. </br>
                    """,
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

    @PostMapping("/messages")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "물어보기 질문 전송",
            description = """
                    ask_home_01 또는 ask_chat_01 화면에서 사용자가 질문을 보낼 때 호출합니다. </br>
                    ACTIVE 세션이 있으면 해당 세션에 USER 메시지를 저장하고, ACTIVE 세션이 없으면 새 세션을 생성한 뒤 USER 메시지를 저장합니다. </br>
                    이 PR 단계에서는 AI 답변을 생성하지 않으므로 ASSISTANT 메시지, followUpQuestions, RAG 근거 문서는 반환하지 않습니다. </br>
                    질문 내용은 앞뒤 공백 제거 후 1자 이상 200자 이하만 허용합니다. </br>
                    answeredTurnCount가 15 이상인 세션에서는 메시지를 저장하지 않고 ASK_CHAT_TURN_LIMIT_EXCEEDED를 반환합니다. </br>
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
                            description = "- ErrorCode: VALIDATION_FAILED - 질문은 공백 제외 1자 이상 200자 이하로 요청해야 함",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(
                            responseCode = "409",
                            description = "- ErrorCode: ASK_CHAT_TURN_LIMIT_EXCEEDED - 세션당 대화 횟수 제한 초과",
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
                request.content()
        );
        return ApiResponseEntity.ok(response);
    }
}
