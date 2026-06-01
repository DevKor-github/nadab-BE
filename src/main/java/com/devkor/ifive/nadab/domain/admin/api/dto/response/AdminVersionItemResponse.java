package com.devkor.ifive.nadab.domain.admin.api.dto.response;

public record AdminVersionItemResponse(
        Long id,
        String title,
        String description,
        Integer displayOrder
) {
}
