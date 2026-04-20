package com.devkor.ifive.nadab.domain.dailyreport.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "답변 이미지 업로드 PresignedURL 생성 요청")
public record CreateAnswerImageUploadUrlRequest(
        @Schema(description = "파일 확장자 (image/png, image/jpeg만 허용)", example = "image/png")
        @NotBlank(message = "파일 확장자는 필수입니다.")
        String contentType
) {
}