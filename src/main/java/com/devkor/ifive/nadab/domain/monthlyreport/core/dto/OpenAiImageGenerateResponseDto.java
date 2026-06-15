package com.devkor.ifive.nadab.domain.monthlyreport.core.dto;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.ai.AiResponseParseException;

import java.util.List;

public record OpenAiImageGenerateResponseDto(
        Long created,
        List<ImageData> data
) {
    public record ImageData(
            String b64_json
    ) {
    }

    public String firstBase64Image() {
        if (data == null || data.isEmpty()) {
            throw new AiResponseParseException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
        String base64 = data.get(0).b64_json();
        if (base64 == null || base64.isBlank()) {
            throw new AiResponseParseException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
        return base64;
    }
}
