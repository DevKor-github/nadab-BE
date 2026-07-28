package com.devkor.ifive.nadab.domain.askchat.api.dto.response;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ask Chat 답변 생성 결과 상태")
public record AskChatAnswerGenerationResponse(
        @Schema(description = "답변 생성 성공 여부", example = "true")
        boolean success,

        @Schema(description = "답변 생성 실패 시 에러 코드. 성공 시 null", example = "AI_RESPONSE_PARSE_FAILED", nullable = true)
        String errorCode,

        @Schema(description = "답변 생성 실패 시 프론트 모달/토스트에 표시할 안내 문구. 성공 시 null", example = "답변 생성에 오류가 발생했어요. 다시 시도해주세요.", nullable = true)
        String message
) {

    public static AskChatAnswerGenerationResponse completed() {
        return new AskChatAnswerGenerationResponse(true, null, null);
    }

    public static AskChatAnswerGenerationResponse failed(ErrorCode errorCode, String message) {
        return new AskChatAnswerGenerationResponse(false, errorCode.getCode(), message);
    }
}
