package com.devkor.ifive.nadab.domain.email.application;

import com.devkor.ifive.nadab.domain.email.core.entity.EmailVerification;
import com.devkor.ifive.nadab.domain.email.core.entity.VerificationType;
import com.devkor.ifive.nadab.domain.email.core.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmailVerificationQueryService {

    private final EmailVerificationRepository emailVerificationRepository;

    // 인증 완료 여부 확인 (회원가입 시 사용)
    public boolean hasValidVerification(String email, VerificationType type) {
        return emailVerificationRepository
                .findByEmailAndVerificationType(email, type)
                .map(EmailVerification::getIsVerified)
                .orElse(false);
    }
}