package com.devkor.ifive.nadab.domain.askchat.api.dto.response;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWallet;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "물어보기 대화권 충전 응답")
public record AskChatTurnChargeResponse(
        @Schema(description = "충전 후 남은 크리스탈 수", example = "70")
        long crystalBalance,

        @Schema(description = "충전 후 사용 가능한 남은 메시지 횟수", example = "12")
        int remainingMessageCount
) {

    public static AskChatTurnChargeResponse of(
            long crystalBalance,
            AskChatWallet askChatWallet
    ) {
        return new AskChatTurnChargeResponse(
                crystalBalance,
                askChatWallet.getTotalTurnBalance()
        );
    }
}
