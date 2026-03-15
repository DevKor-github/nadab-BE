package com.devkor.ifive.nadab.domain.dailyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "홈화면 요약 정보 응답")
public record HomeResponse(
        @Schema(description = "이번 주(월~일) 답변한 날짜 목록 (오름차순)", example = "[\"2026-01-13\", \"2026-01-14\", \"2026-01-15\"]")
        List<LocalDate> answeredDates,

        @Schema(description = "현재 연속 답변 일수 (오늘 답변 있으면 오늘까지, 없으면 어제까지)", example = "15")
        int streakCount,

        @Schema(description = "실제 답변한 날짜의 총 개수", example = "20")
        int totalRecordDays,

        @Schema(description = "나와 같은 오늘의 질문에 답변한 친구들의 프로필 사진 URL (답변 시간 최신순, 최대 5개)",
                example = "[\"https://cdn.example.com/profiles/user1.png\", \"https://cdn.example.com/default/user2.png\"]")
        List<String> answeredFriendProfiles,

        @Schema(description = "나와 같은 오늘의 질문에 답변한 친구의 총 수", example = "8")
        int answeredFriendCount
) {
}