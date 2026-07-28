package com.devkor.ifive.nadab.domain.askchat.api.dto.response;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessage;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageRole;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Ask Chat 메시지 응답")
public record AskChatMessageResponse(
        @Schema(description = "메시지 ID", example = "10")
        Long id,

        @Schema(description = "메시지 역할", example = "USER")
        AskChatMessageRole role,

        @Schema(description = "메시지 상태", example = "COMPLETED")
        AskChatMessageStatus status,

        @Schema(description = "메시지 내용", example = "나는 어떤 사람이야?")
        String content,

        @Schema(description = "메시지 생성 시각")
        OffsetDateTime createdAt
) {

    public static AskChatMessageResponse from(AskChatMessage message) {
        return new AskChatMessageResponse(
                message.getId(),
                message.getRole(),
                message.getStatus(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
