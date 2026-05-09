package com.devkor.ifive.nadab.domain.dailyreport.application;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.request.CreateAnswerImageUploadUrlRequest;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.request.DailyReportRequest;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.CreateAnswerImageUploadUrlResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.CreateDailyReportResponse;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.ImageStatusResponse;
import com.devkor.ifive.nadab.domain.dailyreport.application.event.DailyReportCompletedEvent;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.ConfirmDailyAndRewardDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.PrepareDailyResultDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.AiDailyReportResultDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.ImageStatus;
import com.devkor.ifive.nadab.domain.dailyreport.infra.DailyReportLlmClient;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.entity.UserDailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.repository.DailyQuestionRepository;
import com.devkor.ifive.nadab.domain.question.core.repository.UserDailyQuestionRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.user.core.service.ProfileImageService;
import com.devkor.ifive.nadab.domain.user.infra.ProfileImageUrlBuilder;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;

import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DailyReportService {

    private final UserRepository userRepository;
    private final DailyQuestionRepository dailyQuestionRepository;
    private final UserDailyQuestionRepository userDailyQuestionRepository;

    private final DailyReportTxService dailyReportTxService;
    private final ProfileImageService profileImageService;

    private final DailyReportLlmClient dailyReportLlmClient;

    private final ApplicationEventPublisher eventPublisher;

    private final ProfileImageUrlBuilder profileImageUrlBuilder;

    @Value("${profile-image.env}")
    private String env;

    public CreateDailyReportResponse generateDailyReport(Long userId, DailyReportRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        DailyQuestion question = dailyQuestionRepository.findByIdWithInterest(request.questionId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.QUESTION_NOT_FOUND));

        // objectKey, webpKey 존재 시 검증
        if(!isBlank(request.objectKey())) {
            if(isBlank(request.webpKey())) {
                throw new BadRequestException(ErrorCode.IMAGE_WEBP_KEY_REQUIRED);
            }
             validateWebpKey(request.webpKey(), userId);
        }

        LocalDate today = TodayDateTimeProvider.getTodayDate();

        // 1. 오늘 -> 어제 순서로 조회 (없으면 예외)
        UserDailyQuestion udq = userDailyQuestionRepository.findByUserIdAndDate(userId, today)
                .or(() -> userDailyQuestionRepository.findByUserIdAndDate(userId, today.minusDays(1)))
                .orElseThrow(() -> new NotFoundException(ErrorCode.DAILY_QUESTION_NOT_FOUND));

        // 2. 결과 날짜를 비교하여 플래그 설정
        boolean isDayPassed = !udq.getDate().isEqual(today);

        if (!udq.getDailyQuestion().getId().equals(request.questionId())) {
            throw new BadRequestException(ErrorCode.DAILY_QUESTION_MISMATCH);
        }

        PrepareDailyResultDto prep = dailyReportTxService.prepareDaily(user, question, request.answer(), isDayPassed, request.objectKey());

        AnswerEntry answerEntry = prep.entry();

        AiDailyReportResultDto dto;
        try {
            dto = dailyReportLlmClient.generate(question.getQuestionText(), answerEntry);
        } catch (Exception e) {
            dailyReportTxService.failDaily(prep.reportId());
            throw e;
        }

        ConfirmDailyAndRewardDto confirmDto = dailyReportTxService.confirmDailyAndReward(prep, dto, request.webpKey());

        // 일일 리포트 완성 이벤트 발행 (유형 리포트 제작 가능 알림 체크용)
        if (question.getInterest() != null) {
            InterestCode interestCode = question.getInterest().getCode();
            eventPublisher.publishEvent(
                new DailyReportCompletedEvent(userId, interestCode)
            );
        }

        String imageUrl = answerEntry.getImageKey() != null ? profileImageUrlBuilder.buildUrl(answerEntry.getImageKey()) : null;

        return new CreateDailyReportResponse(
                prep.reportId(),
                dto.message(),
                confirmDto.emotion().getCode().toString(),
                confirmDto.balanceAfter(),
                imageUrl
        );
    }

    public CreateAnswerImageUploadUrlResponse createUploadUrl(
            Long userId,
            CreateAnswerImageUploadUrlRequest request) {

        // content type / 확장자 검증
        String contentType = request.contentType();

        String extension = switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            default -> throw new BadRequestException(ErrorCode.IMAGE_UNSUPPORTED_TYPE);
        };

        String uuid = UUID.randomUUID().toString();
        String objectKey = "%s/answers/original/%d/%s.%s"
                .formatted(env, userId, uuid, extension);

        String uploadUrl = profileImageService.generatePresignedUploadUrl(objectKey, contentType);

        String webpKey = "%s/answers/webp/%d/%s.webp"
                .formatted(env, userId, uuid);

        return new CreateAnswerImageUploadUrlResponse(uploadUrl, objectKey, webpKey);
    }

    public ImageStatusResponse getImageStatus(String key, Long userId) {
        validateWebpKey(key, userId);

        boolean exists = profileImageService.exists(key);

        ImageStatus imageStatus = exists ? ImageStatus.READY : ImageStatus.PROCESSING;

        return new ImageStatusResponse(imageStatus.name());
    }

    private void validateWebpKey(String key, Long userId) {
        if (key == null || key.isBlank()) {
            throw new BadRequestException(ErrorCode.IMAGE_INVALID_KEY);
        }

        String expectedPrefix = "%s/answers/webp/%d/".formatted(env, userId);

        if (!key.startsWith(expectedPrefix)) {
            throw new BadRequestException(ErrorCode.IMAGE_INVALID_KEY);
        }

        if (!key.endsWith(".webp")) {
            throw new BadRequestException(ErrorCode.IMAGE_INVALID_KEY);
        }

        if (key.contains("..")) {
            throw new BadRequestException(ErrorCode.IMAGE_INVALID_KEY);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
