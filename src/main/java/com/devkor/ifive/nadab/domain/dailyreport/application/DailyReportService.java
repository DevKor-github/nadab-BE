package com.devkor.ifive.nadab.domain.dailyreport.application;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.request.DailyReportRequest;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.CreateDailyReportResponse;
import com.devkor.ifive.nadab.domain.dailyreport.application.event.DailyReportCompletedEvent;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.ConfirmDailyAndRewardDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.PrepareDailyResultDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.AiDailyReportResultDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.infra.DailyReportLlmClient;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.entity.UserDailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.repository.DailyQuestionRepository;
import com.devkor.ifive.nadab.domain.question.core.repository.UserDailyQuestionRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;

import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DailyReportService {

    private final UserRepository userRepository;
    private final DailyQuestionRepository dailyQuestionRepository;
    private final UserDailyQuestionRepository userDailyQuestionRepository;

    private final DailyReportTxService dailyReportTxService;

    private final DailyReportLlmClient dailyReportLlmClient;

    private final ApplicationEventPublisher eventPublisher;


    public CreateDailyReportResponse generateDailyReport(Long userId, DailyReportRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        DailyQuestion question = dailyQuestionRepository.findById(request.questionId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.QUESTION_NOT_FOUND));

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

        PrepareDailyResultDto prep = dailyReportTxService.prepareDaily(user, question, request.answer(), isDayPassed);

        AnswerEntry answerEntry = prep.entry();

        AiDailyReportResultDto dto;
        try {
            dto = dailyReportLlmClient.generate(question.getQuestionText(), answerEntry.getContent());
        } catch (Exception e) {
            dailyReportTxService.failDaily(prep.reportId());
            throw e;
        }

        ConfirmDailyAndRewardDto confirmDto = dailyReportTxService.confirmDailyAndReward(prep, dto);

        // 일일 리포트 완성 이벤트 발행 (유형 리포트 제작 가능 알림 체크용)
        if (question.getInterest() != null) {
            InterestCode interestCode = question.getInterest().getCode();
            eventPublisher.publishEvent(
                new DailyReportCompletedEvent(userId, interestCode)
            );
        }

        return new CreateDailyReportResponse(
                prep.reportId(),
                dto.message(),
                confirmDto.emotion().getCode().toString(),
                confirmDto.balanceAfter()
        );
    }
}
