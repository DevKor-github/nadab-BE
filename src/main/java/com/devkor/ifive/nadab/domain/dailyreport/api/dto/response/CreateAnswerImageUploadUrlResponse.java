package com.devkor.ifive.nadab.domain.dailyreport.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "답변 이미지 업로드 PresignedURL 생성 응답")
public record CreateAnswerImageUploadUrlResponse(
        @Schema(description = "답변 이미지 업로드 PresignedURL", example = "https://nadab-profile-images.s3.amazonaws.com/...")
        String uploadUrl,

        @Schema(description = "답변 이미지 Object Key. 일간 리포트 생성에 사용됩니다.", example = "dev/answers/original/12345/092f7ab2-c845-4bdf-8458-e2897135d4e7.png")
        String objectKey
) {
}
