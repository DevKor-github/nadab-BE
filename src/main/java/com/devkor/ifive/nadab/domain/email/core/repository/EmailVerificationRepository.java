package com.devkor.ifive.nadab.domain.email.core.repository;

import com.devkor.ifive.nadab.domain.email.core.entity.EmailVerification;
import com.devkor.ifive.nadab.domain.email.core.entity.VerificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    // 이메일 + 인증 타입으로 조회 (인증 코드 검증, 재발송 시)
    Optional<EmailVerification> findByEmailAndVerificationType(String email, VerificationType verificationType);

    // 재발송 시 기존 레코드 삭제
    @Modifying
    @Query("DELETE FROM EmailVerification ev WHERE ev.email = :email AND ev.verificationType = :verificationType")
    void deleteByEmailAndVerificationType(
            @Param("email") String email,
            @Param("verificationType") VerificationType verificationType
    );
}