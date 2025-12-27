package com.devkor.ifive.nadab.domain.dailyreport.api;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.request.TestDailyReportRequest;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.DailyReportResponse;
import com.devkor.ifive.nadab.domain.dailyreport.application.DailyReportService;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "오늘의 리포트 API", description = "오늘의 리포트 생성 및 조회 관련 API")
@RestController
@RequestMapping("${api_prefix}/daily-report")
@RequiredArgsConstructor
public class DailyReportController {

    private final DailyReportService dailyReportService;

    @PostMapping("/generate/test")
    @PermitAll
    @Operation(
            summary = "(테스트용) 오늘의 리포트 생성 API",
            description = """
                    오늘의 리포트 생성 테스트입니다. 이하의 내용을 지켜 프롬프트를 입력해주세요(기존의 프롬프트를 참고해주세요).
                    1.
                    출력 형식은 반드시 다음과 같도록 프롬프트에 작성해야 합니다:
                    ```json
                    {
                         "message": "(분석 내용)",
                         "emotion": "(감정 키워드)"
                    }
                    ```
                    2.
                    분석 대상을 명시해야 합니다.
                    예시)
                    ```json
                    [분석 대상]
                    질문: {question}
                    답변: {answer}
                    ```
                    이하는 temperature에 대한 설명입니다.<br/>
                    temperature는 AI가 응답을 생성할 때 얼마나 자유롭게(창의적으로) 단어와 표현을 선택할지를 조절하는 값입니다.<br/>
                    값이 낮을수록 항상 비슷하고 예측 가능한 답변을 생성하며, 값이 높을수록 다양한 표현과 새로운 관점이 섞인 답변을 생성합니다.<br/>
                    허용 가능한 값의 범위는 0.0 이상 1.0 이하이며, 일반적으로 0.0에 가까울수록 사실 전달·요약·분석과 같은 정형적인 작업에 적합하고, 0.6 이상부터는 감정 표현이나 공감, 창의적인 문장 생성에 더 적합해집니다.<br/>
                    다만 temperature가 높아질수록 응답의 일관성이 낮아지고, 정해진 형식(JSON 등)을 지키지 못할 가능성도 함께 증가합니다.<br/>
                    따라서 구조화된 결과나 안정적인 응답이 필요한 경우에는 0.0~0.3, 자연스럽고 감정적인 표현이 중요한 경우에는 0.4~0.8 범위 내에서 사용하는 것을 권장합니다.<br/>
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "테스트용 오늘의 리포트 생성 성공",
                            content = @Content(schema = @Schema(implementation = DailyReportResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청"
                    ),
            }
    )
    public ResponseEntity<ApiResponseDto<DailyReportResponse>> generateDailyReport(
            @Valid @RequestBody TestDailyReportRequest request,
            @RequestParam String prompt
    ) {
        DailyReportResponse response = dailyReportService.generateTestDailyReport(request, prompt);
        return ApiResponseEntity.ok(response);
    }
}
