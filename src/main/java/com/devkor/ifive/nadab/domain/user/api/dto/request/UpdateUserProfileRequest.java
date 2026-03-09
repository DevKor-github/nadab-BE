package com.devkor.ifive.nadab.domain.user.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유저 프로필 업데이트 요청")
public record UpdateUserProfileRequest(
        @Schema(
                description = "유저가 설정하고자 하는 새로운 닉네임입니다.",
                example = "코딩하는개발자"
        )
        String nickname,
        @Schema(
                description = "이 값은 presignedURL 생성 API의 응답에서 받은 objectKey여야 합니다. ",
                example = "dev/profiles/original/12345/092f7ab2-c845-4bdf-8458-e2897135d4e7.png"
        )
        String objectKey
) {
}
