package com.devkor.ifive.nadab.domain.admin.api.dto.response;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLog;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLogReason;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWalletLogStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "관리자 Ask Chat 대화권 로그")
public record AdminAskChatWalletLogResponse(
        @Schema(description = "로그 ID", example = "1001")
        Long id,

        @Schema(description = "로그 사용자")
        AdminLogUserResponse user,

        @Schema(description = "Ask Chat 세션 ID", example = "3001")
        Long sessionId,

        @Schema(description = "Ask Chat 메시지 ID", example = "4001")
        Long messageId,

        @Schema(description = "무료 대화권 변동량", example = "-1")
        int freeTurnDelta,

        @Schema(description = "유료 대화권 변동량", example = "0")
        int paidTurnDelta,

        @Schema(description = "변동 후 무료 대화권 잔액", example = "2")
        int freeTurnBalanceAfter,

        @Schema(description = "변동 후 유료 대화권 잔액", example = "10")
        int paidTurnBalanceAfter,

        @Schema(description = "변동 사유", example = "ANSWER_SUCCESS_CONSUME")
        AskChatWalletLogReason reason,

        @Schema(description = "로그 상태", example = "CONFIRMED")
        AskChatWalletLogStatus status,

        @Schema(description = "참조 유형", example = "ASK_CHAT_MESSAGE")
        String refType,

        @Schema(description = "참조 ID", example = "4001")
        Long refId,

        @Schema(description = "멱등성 키", example = "ask-chat-message-4001")
        String idempotencyKey,

        @Schema(description = "로그 생성 시각")
        OffsetDateTime createdAt
) {

    public static AdminAskChatWalletLogResponse from(AskChatWalletLog log) {
        return new AdminAskChatWalletLogResponse(
                log.getId(),
                AdminLogUserResponse.from(log.getUser()),
                log.getSession() == null ? null : log.getSession().getId(),
                log.getMessage() == null ? null : log.getMessage().getId(),
                log.getFreeTurnDelta(),
                log.getPaidTurnDelta(),
                log.getFreeTurnBalanceAfter(),
                log.getPaidTurnBalanceAfter(),
                log.getReason(),
                log.getStatus(),
                log.getRefType(),
                log.getRefId(),
                log.getIdempotencyKey(),
                log.getCreatedAt()
        );
    }
}
