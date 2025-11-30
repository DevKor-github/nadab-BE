package com.devkor.ifive.nadab.domain.email.core.service;

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
public class EmailSendService {

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
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin: 0; padding: 0; background-color: #F8FAFC; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;">
                    <div style="max-width: 600px; margin: 0 auto; background-color: #FFFFFF;">
                        <!-- Header -->
                        <div style="background: linear-gradient(135deg, #3B82F6 0%%, #2563EB 100%%); padding: 32px 24px; text-align: center;">
                            <h1 style="margin: 0; color: #FFFFFF; font-size: 24px; font-weight: 700; letter-spacing: -0.5px;">나답</h1>
                        </div>

                        <!-- Content -->
                        <div style="padding: 40px 24px;">
                            <h2 style="margin: 0 0 16px 0; color: #1E293B; font-size: 20px; font-weight: 600;">%s 인증번호</h2>
                            <p style="margin: 0 0 24px 0; color: #64748B; font-size: 16px; line-height: 1.6;">
                                안녕하세요.<br>
                                요청하신 인증번호를 안내해 드립니다.
                            </p>

                            <!-- Verification Code Box -->
                            <div style="background: linear-gradient(135deg, #EFF6FF 0%%, #DBEAFE 100%%); border: 2px solid #3B82F6; border-radius: 12px; padding: 32px 24px; text-align: center; margin: 32px 0;">
                                <div style="color: #64748B; font-size: 14px; font-weight: 500; margin-bottom: 12px;">인증번호</div>
                                <div style="color: #2563EB; font-size: 36px; font-weight: 700; letter-spacing: 8px; font-family: 'Courier New', monospace;">%s</div>
                            </div>

                            <!-- Notice -->
                            <div style="background-color: #FEF3C7; border-left: 4px solid #F59E0B; padding: 16px; border-radius: 8px; margin: 24px 0;">
                                <p style="margin: 0; color: #92400E; font-size: 14px; line-height: 1.6;">
                                    <strong>유효시간:</strong> 이 인증번호는 <strong>3분간</strong> 유효합니다.
                                </p>
                            </div>

                            <p style="margin: 24px 0 0 0; color: #94A3B8; font-size: 14px; line-height: 1.6;">
                                본인이 요청하지 않은 경우, 이 이메일을 무시해주세요.<br>
                                다른 사람에게 인증번호를 절대 알려주지 마세요.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """, purpose, verificationCode);
    }
}