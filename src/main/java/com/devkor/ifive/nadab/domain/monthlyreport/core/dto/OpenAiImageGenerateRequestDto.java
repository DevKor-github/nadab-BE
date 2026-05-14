package com.devkor.ifive.nadab.domain.monthlyreport.core.dto;

public record OpenAiImageGenerateRequestDto(
        String model,
        String prompt,
        String size,
        String quality,
        String output_format,
        Integer n,
        String user
) {
}
