package com.devkor.ifive.nadab.domain.auth.application;

import com.devkor.ifive.nadab.domain.auth.core.entity.UserWithdrawalReason;
import com.devkor.ifive.nadab.domain.auth.core.entity.WithdrawalReasonType;
import com.devkor.ifive.nadab.domain.auth.core.repository.UserWithdrawalReasonRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceV2 {

    private static final int MAX_CUSTOM_REASON_LENGTH = 200;

    private final WithdrawalService withdrawalService;
    private final UserRepository userRepository;
    private final UserWithdrawalReasonRepository userWithdrawalReasonRepository;

    public void withdrawUser(Long userId, List<WithdrawalReasonType> reasons, String customReason) {
        List<WithdrawalReasonType> validatedReasons = validateReasons(reasons);
        String normalizedCustomReason = normalizeCustomReason(customReason);
        validateCustomReason(validatedReasons, normalizedCustomReason);

        // 기존 탈퇴 처리(소프트 삭제/토큰 revoke/Apple revoke)
        withdrawalService.withdrawUser(userId);

        // 탈퇴 사유 저장(집계용)
        User user = userRepository.getReferenceById(userId);
        OffsetDateTime effectiveWithdrawnAt = user.getDeletedAt() != null
                ? user.getDeletedAt()
                : OffsetDateTime.now();
        List<UserWithdrawalReason> entities = new ArrayList<>(validatedReasons.size());
        for (WithdrawalReasonType reason : validatedReasons) {
            String detail = reason == WithdrawalReasonType.OTHER ? normalizedCustomReason : null;
            entities.add(UserWithdrawalReason.create(
                    user,
                    reason,
                    detail,
                    effectiveWithdrawnAt
            ));
        }
        userWithdrawalReasonRepository.saveAll(entities);
    }

    private List<WithdrawalReasonType> validateReasons(List<WithdrawalReasonType> reasons) {
        if (reasons == null || reasons.isEmpty()) {
            throw new BadRequestException(ErrorCode.AUTH_WITHDRAWAL_REASON_REQUIRED);
        }

        Set<WithdrawalReasonType> uniqueReasons = EnumSet.copyOf(reasons);
        if (uniqueReasons.size() != reasons.size()) {
            throw new BadRequestException(ErrorCode.AUTH_WITHDRAWAL_REASON_DUPLICATED);
        }
        return reasons;
    }

    private void validateCustomReason(List<WithdrawalReasonType> reasons, String customReason) {
        boolean hasOther = reasons.contains(WithdrawalReasonType.OTHER);

        if (hasOther) {
            if (customReason == null || customReason.isEmpty()) {
                throw new BadRequestException(ErrorCode.AUTH_WITHDRAWAL_OTHER_REASON_REQUIRED);
            }
            if (customReason.length() > MAX_CUSTOM_REASON_LENGTH) {
                throw new BadRequestException(ErrorCode.AUTH_WITHDRAWAL_OTHER_REASON_TOO_LONG);
            }
            return;
        }

        if (customReason != null && !customReason.isEmpty()) {
            throw new BadRequestException(ErrorCode.AUTH_WITHDRAWAL_OTHER_REASON_NOT_ALLOWED);
        }
    }

    private String normalizeCustomReason(String customReason) {
        if (customReason == null) {
            return null;
        }
        return customReason.trim();
    }
}
