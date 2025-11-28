package com.devkor.ifive.nadab.domain.user.application;

import com.devkor.ifive.nadab.domain.user.api.dto.request.CreateProfileImageUploadUrlRequest;
import com.devkor.ifive.nadab.domain.user.api.dto.response.CreateProfileImageUploadUrlResponse;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileImageService {

    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${profile-image.env}")
    private String env;

    // 프로필 이미지 업로드 presigned URL 발급
    public CreateProfileImageUploadUrlResponse createUploadUrl(Long userId,
                                                               CreateProfileImageUploadUrlRequest request) {
        // content type / 확장자 검증
        String contentType = request.contentType();
        if (!"image/jpeg".equalsIgnoreCase(contentType)
                && !"image/png".equalsIgnoreCase(contentType)) {
            throw new BadRequestException("지원하지 않는 이미지 형식입니다. (jpg, png만 허용)");
        }

        String extension = switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png"  -> "png";
            default -> throw new BadRequestException("지원하지 않는 이미지 형식입니다.");
        };

        String uuid = UUID.randomUUID().toString();
        String objectKey = "%s/profiles/%d/%s.%s"
                .formatted(env, userId, uuid, extension);

        // PutObjectRequest 생성
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .build();

        // Presign 요청 (5분 유효)
        PresignedPutObjectRequest presignedRequest =
                s3Presigner.presignPutObject(builder -> builder
                        .signatureDuration(Duration.ofMinutes(5))
                        .putObjectRequest(objectRequest));

        String url = presignedRequest.url().toString();

        return new CreateProfileImageUploadUrlResponse(url, objectKey);
    }
}

