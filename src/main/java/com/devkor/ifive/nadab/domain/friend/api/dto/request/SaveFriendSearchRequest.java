package com.devkor.ifive.nadab.domain.friend.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "친구 검색 기록 저장 요청")
public record SaveFriendSearchRequest(
        @Schema(description = "검색된 유저의 닉네임", example = "춤추는사막여우")
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 1, max = 255, message = "닉네임은 1자 이상 255자 이하여야 합니다.")
        String nickname
) {
}