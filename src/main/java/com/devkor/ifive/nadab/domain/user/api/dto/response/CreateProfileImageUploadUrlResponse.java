package com.devkor.ifive.nadab.domain.user.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로필 이미지 업로드 PresignedURL 생성 응답")
public record CreateProfileImageUploadUrlResponse(
        @Schema(description = "프로필 이미지 업로드 PresignedURL", example = "https://nadab-profile-images.s3.amazonaws.com/...")
        String uploadUrl,

        @Schema(description = "프로필 이미지 Object Key. 프로필 업데이트에 사용됩니다.", example = "dev/profiles/original/12345/092f7ab2-c845-4bdf-8458-e2897135d4e7.png")
        String objectKey
) {
}
