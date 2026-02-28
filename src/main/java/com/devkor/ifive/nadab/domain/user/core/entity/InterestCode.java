package com.devkor.ifive.nadab.domain.user.core.entity;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;

/**
 * 관심 주제 코드
 */
public enum InterestCode {
    PREFERENCE,
    EMOTION,
    ROUTINE,
    RELATIONSHIP,
    LOVE,
    VALUES;

    public static InterestCode fromString(String code) {
        for (InterestCode interestCode : InterestCode.values()) {
            if (interestCode.name().equalsIgnoreCase(code)) {
                return interestCode;
            }
        }
        throw new BadRequestException(ErrorCode.USER_INTEREST_CODE_INVALID);
    }

    public String displayNameKo() {
        return switch (this) {
            case PREFERENCE -> "취향";
            case EMOTION -> "감정";
            case ROUTINE -> "루틴";
            case RELATIONSHIP -> "인간관계";
            case LOVE -> "사랑";
            case VALUES -> "가치관";
        };
    }
}
