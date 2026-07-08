package com.devkor.ifive.nadab.domain.admin.api.dto.response;

import com.devkor.ifive.nadab.domain.appversion.core.entity.AppPlatform;

import java.time.OffsetDateTime;
import java.util.List;

public record AdminVersionHistoryItemResponse(
        Long id,
        AppPlatform platform,
        String version,
        Boolean isLatest,
        String summary,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<AdminVersionItemResponse> items
) {
}
