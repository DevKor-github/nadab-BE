package com.devkor.ifive.nadab.global.exception;

import com.devkor.ifive.nadab.domain.auth.api.dto.response.WithdrawnInfoResponse;
import lombok.Getter;

/**
 * 탈퇴 계정 예외
 * - 탈퇴한 계정으로 로그인 시도 시 발생
 * - 계정 정보를 포함하여 프론트에 전달
 */
@Getter
public class WithdrawnException extends RuntimeException {
    private final WithdrawnInfoResponse withdrawnInfo;

    public WithdrawnException(String message, WithdrawnInfoResponse withdrawnInfo) {
        super(message);
        this.withdrawnInfo = withdrawnInfo;
    }
}