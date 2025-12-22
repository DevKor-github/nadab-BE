package com.devkor.ifive.nadab.domain.auth.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * 탈퇴 계정 정보 DTO
 * - 에러 응답
 * - 로그인 실패 시 탈퇴 계정 정보를 반환
 */
@Schema(description = "탈퇴 계정 정보")
public record WithdrawnInfoResponse(
        @Schema(description = "사용자 닉네임", example = "홍길동")
        String nickname,

        @Schema(description = "완전 삭제 예정일 (탈퇴 일시 + 14일)", example = "2024-01-29")
        LocalDate deletionDate
) {
}