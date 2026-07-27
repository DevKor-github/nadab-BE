package com.devkor.ifive.nadab.domain.pdfexport.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * PDF 결과물 S3 저장 + CloudFront signed URL 다운로드.
 * - 업로드: 렌더 결과 임시파일을 열거불가 키로 저장(파일명 Content-Disposition 각인 포함)
 * - 다운로드: CloudFront signed URL 발급(COMPLETED 조회 시). S3는 OAC로 은닉, 엣지가 서명 검증
 * - 삭제: dedup(동일 기간 재생성 시 이전 결과물 제거)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PdfExportStorage {

    private final S3Client s3Client;

    private final CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${profile-image.env}")
    private String env;

    /** 서명 URL 오리진. */
    @Value("${profile-image.base-url}")
    private String cloudFrontDomain;

    /** CloudFront에 등록한 Key Pair ID(서명 URL 검증 키 식별자). */
    @Value("${pdf-export.cloudfront.key-pair-id}")
    private String keyPairId;

    /** 서명용 RSA private key(PKCS#8 PEM). */
    @Value("${pdf-export.cloudfront.private-key}")
    private String privateKeyPem;

    /** 서명 수명(=다운로드 시작 창). 만료는 요청 시작 시점에만 검사돼 시작된 전송은 완주. 짧게 잡아 재발급 강제, 보관 7일과는 다른 층. */
    private static final Duration PRESIGN_TTL = Duration.ofMinutes(3);

    /** PEM 파싱은 비싸지 않지만 불변이라 최초 서명 시 한 번만 파싱해 캐시. */
    private volatile PrivateKey cachedPrivateKey;

    /** 열거불가 결과 키(UUID로 순차 열거 차단). {env}/pdf-exports/{userId}/{uuid}.pdf */
    public String newResultKey(Long userId) {
        return "%s/pdf-exports/%d/%s.pdf".formatted(env, userId, UUID.randomUUID());
    }

    /**
     * 파일명 Content-Disposition을 업로드 시 각인한다 — CloudFront signed URL은 발급 시 헤더를 못 싣기 때문.
     * PDF는 임시파일에서 스트리밍 업로드한다(힙에 전체 바이트를 올리지 않는다).
     */
    public void upload(String key, Path pdfFile, String downloadFilename, String asciiFallbackFilename) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("application/pdf")
                .contentDisposition(buildContentDisposition(downloadFilename, asciiFallbackFilename))
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromFile(pdfFile));
    }

    /** 답변 사진 원본 바이트(리스너 렌더용, 전체 객체 키). 무거운 I/O라 Tx/커넥션 밖에서 호출. */
    public byte[] download(String key) {
        return s3Client.getObjectAsBytes(builder -> builder.bucket(bucket).key(key)).asByteArray();
    }

    /** dedup으로 밀려난 이전 결과물 삭제. best-effort — 실패해도 7일 lifecycle이 정리하므로 예외를 삼킨다. */
    public void delete(String key) {
        try {
            s3Client.deleteObject(builder -> builder.bucket(bucket).key(key));
        } catch (RuntimeException e) {
            log.warn("[PDF_EXPORT] PDF 결과물 S3 삭제 실패(lifecycle이 정리할 예정): key={}", key, e);
        }
    }

    /** CloudFront signed URL(canned policy) 발급. 도메인+키만 가리켜 S3 미노출, 파일명은 업로드 때 각인됨. */
    public String generateSignedGetUrl(String key) {
        String resourceUrl = "%s/%s".formatted(cloudFrontDomain, key);

        CannedSignerRequest signerRequest = CannedSignerRequest.builder()
                .resourceUrl(resourceUrl)
                .privateKey(privateKey())
                .keyPairId(keyPairId)
                .expirationDate(Instant.now().plus(PRESIGN_TTL))
                .build();

        SignedUrl signedUrl = cloudFrontUtilities.getSignedUrlWithCannedPolicy(signerRequest);
        return signedUrl.url();
    }

    /** 다운로드 파일명 Content-Disposition 헤더. 한글은 RFC 5987(filename*=UTF-8''), ASCII 폴백(filename) 병기. */
    private String buildContentDisposition(String downloadFilename, String asciiFallbackFilename) {
        String encoded = URLEncoder.encode(downloadFilename, StandardCharsets.UTF_8).replace("+", "%20");
        return "attachment; filename=\"%s\"; filename*=UTF-8''%s".formatted(asciiFallbackFilename, encoded);
    }

    /** PKCS#8 PEM private key를 최초 1회 파싱해 캐시(double-checked locking). */
    private PrivateKey privateKey() {
        PrivateKey key = cachedPrivateKey;
        if (key == null) {
            synchronized (this) {
                key = cachedPrivateKey;
                if (key == null) {
                    key = loadPrivateKey(privateKeyPem);
                    cachedPrivateKey = key;
                }
            }
        }
        return key;
    }

    private PrivateKey loadPrivateKey(String pem) {
        try {
            String base64 = pem
                    .replace("\\n", "\n")
                    .replaceAll("-+BEGIN[A-Z ]*PRIVATE KEY-+", "")
                    .replaceAll("-+END[A-Z ]*PRIVATE KEY-+", "")
                    .replaceAll("\\s", "");
            byte[] der = Base64.getDecoder().decode(base64);
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("PDF 다운로드용 CloudFront private key 파싱 실패(PKCS#8 PEM 확인)", e);
        }
    }
}