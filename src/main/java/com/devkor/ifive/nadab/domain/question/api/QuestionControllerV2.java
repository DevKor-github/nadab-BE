package com.devkor.ifive.nadab.domain.question.api;

import com.devkor.ifive.nadab.domain.question.api.dto.response.DailyQuestionResponseV2;
import com.devkor.ifive.nadab.domain.question.application.QuestionCommandServiceV2;
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

@Tag(name = "질문 API V2", description = "오늘의 질문 관련 API V2")
@RestController
@RequestMapping("/api/v2/question")
@RequiredArgsConstructor
public class QuestionControllerV2 {

    private final QuestionCommandServiceV2 questionCommandService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "오늘의 질문 조회",
            description = "오늘의 질문을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(schema = @Schema(implementation = DailyQuestionResponseV2.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패 (JWT 토큰 관련)",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = """
                                    - ErrorCode: USER_NOT_FOUND - 사용자를 찾을 수 없습니다.
                                    - ErrorCode: USER_INTEREST_NOT_FOUND - 관심 주제를 찾을 수 없습니다.
                                    - ErrorCode: QUESTION_NOT_FOUND_FOR_CONDITION - 조건에 맞는 질문을 찾을 수 없습니다.
                                    """,
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<DailyQuestionResponseV2>> getDailyQuestion(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        DailyQuestionResponseV2 response = questionCommandService.getOrCreateTodayQuestion(principal.getId());
        return ApiResponseEntity.ok(response);
    }

    @PostMapping("/reroll")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "새로운 질문 받기",
            description = "오늘의 질문을 새로 받습니다. 하루에 5번까지 가능합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(schema = @Schema(implementation = DailyQuestionResponseV2.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "사용자 인증 실패",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = """
                                    - ErrorCode: DAILY_QUESTION_NOT_FOUND - 오늘의 질문이 아직 생성되지 않았습니다.
                                    - ErrorCode: USER_INTEREST_NOT_FOUND - 유저의 관심 주제를 찾을 수 없습니다.
                                    - ErrorCode: QUESTION_NO_ALTERNATIVE - 리롤 가능한 질문이 없습니다.
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = """
                                    - ErrorCode: QUESTION_REROLL_LIMIT_EXCEEDED - 오늘의 질문은 하루에 5번까지만 새로 받을 수 있습니다.
                                    - ErrorCode: QUESTION_ALREADY_ANSWERED - 오늘의 질문에 이미 답변을 작성함
                                    """,
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<DailyQuestionResponseV2>> rerollDailyQuestion(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        DailyQuestionResponseV2 response = questionCommandService.rerollTodayQuestion(principal.getId());
        return ApiResponseEntity.ok(response);
    }
}
