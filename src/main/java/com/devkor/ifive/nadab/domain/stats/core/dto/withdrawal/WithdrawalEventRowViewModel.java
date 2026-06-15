package com.devkor.ifive.nadab.domain.stats.core.dto.withdrawal;

public record WithdrawalEventRowViewModel(
        String withdrawnAt,
        String reasons,
        String customReason
) {
}
