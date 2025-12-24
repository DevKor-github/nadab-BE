package com.devkor.ifive.nadab.domain.wallet.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지갑 잔액 응답")
public record WalletBalanceResponse(
        long crystalBalance
) {
}
