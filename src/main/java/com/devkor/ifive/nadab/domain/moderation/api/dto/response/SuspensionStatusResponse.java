package com.devkor.ifive.nadab.domain.moderation.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "소셜 정지 상태 응답")
public record SuspensionStatusResponse(

        @Schema(description = "소셜 정지 여부")
        boolean isSuspended,

        @Schema(description = "정지 해제 시각 (정지 중일 때만 반환)", nullable = true)
        OffsetDateTime expiresAt
) {
    public static SuspensionStatusResponse notSuspended() {
        return new SuspensionStatusResponse(false, null);
    }

    public static SuspensionStatusResponse suspended(OffsetDateTime expiresAt) {
        return new SuspensionStatusResponse(true, expiresAt);
    }
}