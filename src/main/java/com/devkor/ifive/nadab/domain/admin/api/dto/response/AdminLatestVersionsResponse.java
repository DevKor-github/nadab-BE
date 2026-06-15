package com.devkor.ifive.nadab.domain.admin.api.dto.response;

import java.util.List;

public record AdminLatestVersionsResponse(
        List<AdminVersionResponse> versions
) {
}
