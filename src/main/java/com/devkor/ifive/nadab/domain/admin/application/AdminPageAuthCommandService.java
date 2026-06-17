package com.devkor.ifive.nadab.domain.admin.application;

import com.devkor.ifive.nadab.domain.admin.core.properties.AdminPageProperties;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
@RequiredArgsConstructor
public class AdminPageAuthCommandService {

    private final AdminPageProperties adminPageProperties;

    public void validatePassword(String rawPassword) {
        byte[] input = rawPassword.strip().getBytes(StandardCharsets.UTF_8);
        byte[] expected = adminPageProperties.getPassword().strip().getBytes(StandardCharsets.UTF_8);

        if (!MessageDigest.isEqual(input, expected)) {
            throw new UnauthorizedException(ErrorCode.ADMIN_PAGE_INVALID_PASSWORD);
        }
    }
}
