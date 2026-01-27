package com.devkor.ifive.nadab.domain.friend.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "유저 검색 요청")
public record SearchUserRequest(
        @Schema(description = "검색 키워드 (닉네임)", example = "모래")
        @NotBlank(message = "검색 키워드는 필수입니다.")
        @Size(min = 1, max = 255, message = "검색 키워드는 1자 이상 255자 이하여야 합니다.")
        String keyword,

        @Schema(description = "다음 페이지 커서 (형식: nickname)", example = "모래가나")
        String cursor
) {
}