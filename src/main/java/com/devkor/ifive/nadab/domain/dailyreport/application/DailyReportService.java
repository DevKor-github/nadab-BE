package com.devkor.ifive.nadab.domain.dailyreport.application;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.request.DailyReportRequest;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.CreateDailyReportResponse;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.ConfirmDailyAndRewardDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.PrepareDailyResultDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.AiDailyReportResultDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.infra.DailyReportLlmClient;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.entity.UserDailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.repository.DailyQuestionRepository;
import com.devkor.ifive.nadab.domain.question.core.repository.UserDailyQuestionRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;

import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import lombok.RequiredArgsConstructor;
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


    public CreateDailyReportResponse generateDailyReport(Long userId, DailyReportRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. id: " + userId));

        DailyQuestion question = dailyQuestionRepository.findById(request.questionId())
                .orElseThrow(() -> new NotFoundException("질문을 찾을 수 없습니다. id: " + request.questionId()));

        LocalDate today = TodayDateTimeProvider.getTodayDate();
        UserDailyQuestion udq = userDailyQuestionRepository.findByUserIdAndDate(userId, today)
                .orElseThrow(() -> new BadRequestException("오늘의 질문이 사용자에게 할당되지 않았습니다. date: " + today));
        if (!udq.getDailyQuestion().getId().equals(request.questionId())) {
            throw new BadRequestException("요청의 질문이 사용자에게 할당된 오늘의 질문과 일치하지 않습니다. 할당된 questionId: " + udq.getDailyQuestion().getId());
        }

        PrepareDailyResultDto prep = dailyReportTxService.prepareDaily(user, question, request.answer());

        AnswerEntry answerEntry = prep.entry();

        AiDailyReportResultDto dto;
        try {
            dto = dailyReportLlmClient.generate(question.getQuestionText(), answerEntry.getContent());
        } catch (Exception e) {
            dailyReportTxService.failDaily(prep.reportId());
            throw e;
        }

        ConfirmDailyAndRewardDto confirmDto = dailyReportTxService.confirmDailyAndReward(prep, dto);

        return new CreateDailyReportResponse(
                prep.reportId(),
                dto.message(),
                confirmDto.emotion().getCode().toString(),
                confirmDto.balanceAfter()
        );
    }
}
