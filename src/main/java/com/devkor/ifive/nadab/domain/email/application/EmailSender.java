package com.devkor.ifive.nadab.domain.email.application;

import com.devkor.ifive.nadab.domain.email.core.entity.VerificationType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    // 인증 이메일 발송 (비동기)
    @Async("emailTaskExecutor")
    public void sendVerificationEmail(String toEmail, String verificationCode, VerificationType type) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject(getSubject(type));
            helper.setText(getEmailBody(verificationCode, type), true);

            mailSender.send(message);
            log.info("이메일 발송 성공: {} (타입: {})", toEmail, type);

        } catch (MessagingException e) {
            log.error("이메일 발송 실패: {} (타입: {})", toEmail, type, e);
        }
    }

    // 이메일 제목 생성
    private String getSubject(VerificationType type) {
        return switch (type) {
            case SIGNUP -> "[나답] 회원가입 인증번호";
            case PASSWORD_RESET -> "[나답] 비밀번호 재설정 인증번호";
        };
    }

    // 이메일 본문 생성 (HTML)
    private String getEmailBody(String verificationCode, VerificationType type) {
        String purpose = switch (type) {
            case SIGNUP -> "회원가입";
            case PASSWORD_RESET -> "비밀번호 재설정";
        };

        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                        <h2 style="color: #4CAF50;">나답 %s 인증번호</h2>
                        <p>안녕하세요,</p>
                        <p>%s을 위한 인증번호는 다음과 같습니다:</p>
                        <div style="background-color: #f4f4f4; padding: 15px; border-radius: 5px; text-align: center; margin: 20px 0;">
                            <h1 style="color: #4CAF50; margin: 0; letter-spacing: 5px;">%s</h1>
                        </div>
                        <p>이 인증번호는 <strong>3분간 유효</strong>합니다.</p>
                        <p>본인이 요청하지 않은 경우, 이 이메일을 무시해주세요.</p>
                        <hr style="border: none; border-top: 1px solid #ddd; margin: 20px 0;">
                        <p style="font-size: 12px; color: #999;">이 이메일은 자동 발송되었습니다.</p>
                    </div>
                </body>
                </html>
                """, purpose, purpose, verificationCode);
    }
}