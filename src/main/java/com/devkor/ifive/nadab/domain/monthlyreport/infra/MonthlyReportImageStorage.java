package com.devkor.ifive.nadab.domain.monthlyreport.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Base64;

@Component
@RequiredArgsConstructor

public class MonthlyReportImageStorage {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${profile-image.env}")
    private String env;

    public String uploadBase64Webp(Long userId, Long reportId, String base64Image) {
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);

        String key = buildKey(userId, reportId);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("image/webp")
                .cacheControl("public, max-age=31536000, immutable")
                .build();

        s3Client.putObject(
                putObjectRequest,
                RequestBody.fromBytes(imageBytes)
        );

        return key;
    }

    private String buildKey(Long userId, Long reportId) {
        return "%s/reports/monthly/images/%d/%d.webp"
                .formatted(env, userId, reportId);
    }
}
