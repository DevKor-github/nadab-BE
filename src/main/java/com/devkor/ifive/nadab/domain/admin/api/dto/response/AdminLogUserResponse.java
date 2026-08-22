package com.devkor.ifive.nadab.domain.admin.api.dto.response;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 로그 사용자 정보")
public record AdminLogUserResponse(
        @Schema(description = "사용자 ID", example = "101")
        Long id,

        @Schema(description = "닉네임", example = "nadab_user")
        String nickname,

        @Schema(description = "이메일", example = "user@example.com")
        String email
) {

    public static AdminLogUserResponse from(User user) {
        if (user == null) {
            return null;
        }

        return new AdminLogUserResponse(
                user.getId(),
                user.getNickname(),
                user.getEmail()
        );
    }
}
