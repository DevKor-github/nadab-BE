package com.devkor.ifive.nadab.domain.question.api;

import com.devkor.ifive.nadab.domain.question.api.dto.response.DailyQuestionResponse;
import com.devkor.ifive.nadab.domain.question.application.QuestionCommandService;
import com.devkor.ifive.nadab.domain.question.application.QuestionQueryService;
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

@Tag(name = "질문 API", description = "오늘의 질문 관련 API")
@RestController
@RequestMapping("${api_prefix}/questions/")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionCommandService questionCommandService;

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
                            content = @Content(schema = @Schema(implementation = DailyQuestionResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "사용자 인증 실패",
                            content = @Content
                    ),
            }
    )
    public ResponseEntity<ApiResponseDto<DailyQuestionResponse>> getDailyQuestion(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        DailyQuestionResponse response = questionCommandService.getOrCreateTodayQuestion(principal.getId());
        return ApiResponseEntity.ok(response);
    }
}
