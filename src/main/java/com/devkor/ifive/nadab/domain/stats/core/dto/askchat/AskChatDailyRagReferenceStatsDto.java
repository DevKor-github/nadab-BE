package com.devkor.ifive.nadab.domain.stats.core.dto.askchat;

import java.time.LocalDate;

public record AskChatDailyRagReferenceStatsDto(
        LocalDate date,
        long referenceCount,
        long uniqueReferencedDocumentCount
) {
}
