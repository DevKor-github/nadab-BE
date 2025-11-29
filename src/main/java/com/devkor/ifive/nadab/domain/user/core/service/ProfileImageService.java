package com.devkor.ifive.nadab.domain.user.core.service;

import com.devkor.ifive.nadab.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileImageService {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${profile-image.env}")
    private String env;

    // 프로필 이미지 업로드 presigned URL 발급
    public String generatePresignedUploadUrl(String objectKey, String contentType) {

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
        return url;
    }

    /**
     * S3 이미지 삭제 메소드
     */
    public void deleteProfileImage(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return; // 아무것도 삭제할 필요 없음
        }

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    /**
     * 업로드된 이미지의 유효성 검사
     */
    public void checkImageValidity(String objectKey) {
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        HeadObjectResponse metadata = s3Client.headObject(headObjectRequest);

        String contentType = metadata.contentType();
        Long size = metadata.contentLength();

        if (size == null || contentType == null || !List.of("image/jpeg", "image/png", "image/webp").contains(contentType)) {
            throw new BadRequestException("지원하지 않는 이미지 형식 또는 메타데이터가 누락되었습니다.");
        }
        if (size > 5 * 1024 * 1024) {
            this.deleteProfileImage(objectKey);
            throw new BadRequestException("이미지 용량이 너무 큽니다. (최대 5MB)");
        }
    }
}

