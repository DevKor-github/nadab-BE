package com.devkor.ifive.nadab.domain.askchat.core.dto;

public record AskChatTurnReservation(
        Long walletLogId,
        int freeTurnDelta,
        int paidTurnDelta
) {

    public boolean usedFreeTurn() {
        return freeTurnDelta < 0;
    }

    public boolean usedPaidTurn() {
        return paidTurnDelta < 0;
    }
}
