package com.devkor.ifive.nadab.domain.admin.api.dto.response;

import com.devkor.ifive.nadab.domain.appversion.core.entity.AppPlatform;

import java.util.List;

public record AdminVersionResponse(
        Long id,
        AppPlatform platform,
        String version,
        String summary,
        List<AdminVersionItemResponse> items
) {
}
