package com.devkor.ifive.nadab.domain.askchat.api.dto.response;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatWallet;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "물어보기 대화권 충전 응답")
public record AskChatTurnChargeResponse(
        @Schema(description = "충전된 대화권 수", example = "10")
        int chargedTurnCount,

        @Schema(description = "차감된 크리스탈 수", example = "200")
        long crystalCost,

        @Schema(description = "충전 후 남은 크리스탈 수", example = "70")
        long crystalBalance,

        @Schema(description = "충전 후 무료 대화권 잔여 수", example = "2")
        int freeTurnBalance,

        @Schema(description = "충전 후 유료 대화권 잔여 수", example = "10")
        int paidTurnBalance,

        @Schema(description = "충전 후 전체 대화권 잔여 수", example = "12")
        int totalTurnBalance
) {

    public static AskChatTurnChargeResponse of(
            int chargedTurnCount,
            long crystalCost,
            long crystalBalance,
            AskChatWallet askChatWallet
    ) {
        return new AskChatTurnChargeResponse(
                chargedTurnCount,
                crystalCost,
                crystalBalance,
                askChatWallet.getFreeTurnBalance(),
                askChatWallet.getPaidTurnBalance(),
                askChatWallet.getTotalTurnBalance()
        );
    }
}
