package com.devkor.ifive.nadab.domain.admin.application;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record AdminLogSearchCondition(
        int page,
        int size,
        String nickname,
        String email
) {

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 50;

    public AdminLogSearchCondition {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BadRequestException(ErrorCode.VALIDATION_FAILED);
        }

        nickname = normalize(nickname);
        email = normalize(email);
    }

    public static AdminLogSearchCondition of(
            int page,
            int size,
            String nickname,
            String email
    ) {
        return new AdminLogSearchCondition(page, size, nickname, email);
    }

    public Pageable toPageable() {
        Sort sort = Sort.by(
                Sort.Order.desc("createdAt"),
                Sort.Order.desc("id")
        );
        return PageRequest.of(page - 1, size, sort);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
