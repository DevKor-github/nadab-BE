package com.devkor.ifive.nadab.domain.email.application;

import com.devkor.ifive.nadab.domain.email.core.entity.EmailVerification;
import com.devkor.ifive.nadab.domain.email.core.entity.VerificationType;
import com.devkor.ifive.nadab.domain.email.core.repository.EmailVerificationRepository;
import com.devkor.ifive.nadab.domain.email.core.service.EmailSendService;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EmailVerificationCommandService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailSendService emailSendService;

    // 인증 코드 발송
    public void sendVerificationCode(String email, VerificationType type) {
        // 기존 레코드 삭제 (재발송 대응)
        emailVerificationRepository.deleteByEmailAndVerificationType(email, type);

        // 6자리 랜덤 코드 생성
        String verificationCode = generateVerificationCode();

        // DB 저장
        EmailVerification verification = EmailVerification.create(email, type, verificationCode);
        emailVerificationRepository.save(verification);

        // 비동기 이메일 발송
        emailSendService.sendVerificationEmail(email, verificationCode, type);

        log.info("인증 코드 발송: email={}, type={}", email, type);
    }

    // 인증 코드 검증
    public void verifyCode(String email, String code, VerificationType type) {
        // 인증 레코드 조회
        EmailVerification verification = emailVerificationRepository
                .findByEmailAndVerificationType(email, type)
                .orElseThrow(() -> new NotFoundException("인증 요청을 찾을 수 없습니다."));

        // 이미 인증 완료된 경우
        if (verification.getIsVerified()) {
            throw new BadRequestException("이미 인증이 완료되었습니다.");
        }

        // 만료 확인
        if (verification.isExpired()) {
            throw new BadRequestException("인증 코드가 만료되었습니다. 재발송을 요청해주세요.");
        }

        // 코드 검증
        if (!verification.matchesCode(code)) {
            throw new BadRequestException("인증 코드가 일치하지 않습니다.");
        }

        // 인증 완료 처리
        verification.completeVerification();

        log.info("인증 코드 검증 성공: email={}, type={}", email, type);
    }

    // 6자리 랜덤 숫자 생성 (100000 ~ 999999)
    private String generateVerificationCode() {
        int code = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(code);
    }
}