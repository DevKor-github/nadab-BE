package com.devkor.ifive.nadab.domain.askchat.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "물어보기 홈 화면 응답")
public record AskChatHomeResponse(
        @Schema(description = "사용자가 사용할 수 있는 남은 메시지 횟수. 무료/유료 대화권을 합산한 값입니다.", example = "9")
        int remainingMessageCount,

        @Schema(description = "홈 화면 인트로 문구에 표시할 사용자 닉네임", example = "현진")
        String nickname,

        @Schema(description = "사용자가 보유한 크리스탈 개수", example = "100")
        long crystalBalance,

        @Schema(description = "홈 화면에 표시할 예시 질문 목록. 여러 주제 중 일부를 랜덤으로 제공합니다.")
        List<AskChatSampleQuestionResponse> sampleQuestions
) {
}
