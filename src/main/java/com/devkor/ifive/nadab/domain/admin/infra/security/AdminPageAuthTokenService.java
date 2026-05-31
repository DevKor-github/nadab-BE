package com.devkor.ifive.nadab.domain.admin.infra.security;

import com.devkor.ifive.nadab.domain.admin.core.properties.AdminPageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class AdminPageAuthTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final AdminPageProperties adminPageProperties;

    public String issueToken() {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + adminPageProperties.getTokenExpirationSeconds();

        String payload = issuedAt + ":" + expiresAt;
        String encodedPayload = encode(payload.getBytes(StandardCharsets.UTF_8));
        String signature = sign(encodedPayload);

        return encodedPayload + "." + signature;
    }

    public boolean isValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            return false;
        }

        String encodedPayload = parts[0];
        String signature = parts[1];
        String expectedSignature = sign(encodedPayload);

        if (!MessageDigest.isEqual(
                signature.getBytes(StandardCharsets.UTF_8),
                expectedSignature.getBytes(StandardCharsets.UTF_8)
        )) {
            return false;
        }

        String decodedPayload;
        try {
            decodedPayload = new String(
                    Base64.getUrlDecoder().decode(encodedPayload),
                    StandardCharsets.UTF_8
            );
        } catch (IllegalArgumentException e) {
            return false;
        }

        String[] payloadParts = decodedPayload.split(":");
        if (payloadParts.length != 2) {
            return false;
        }

        long expiresAt;
        try {
            expiresAt = Long.parseLong(payloadParts[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        return Instant.now().getEpochSecond() < expiresAt;
    }

    public String getCookieName() {
        return adminPageProperties.getCookieName();
    }

    private String sign(String encodedPayload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(
                    adminPageProperties.getPassword().getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM
            ));
            byte[] signature = mac.doFinal(encodedPayload.getBytes(StandardCharsets.UTF_8));
            return encode(signature);
        } catch (Exception e) {
            throw new IllegalStateException("Admin token signing failed", e);
        }
    }

    private String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
