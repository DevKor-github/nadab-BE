package com.devkor.ifive.nadab.domain.dailyreport.application;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.request.DailyReportRequest;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.DailyReportResponse;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.PrepareDailyResultDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.AiReportResultDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.domain.dailyreport.infra.DailyReportLlmClient;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.repository.DailyQuestionRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.global.exception.NotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DailyReportService {

    private final UserRepository userRepository;
    private final DailyQuestionRepository dailyQuestionRepository;
    private final DailyReportRepository dailyReportRepository;

    private final DailyReportTxService dailyReportTxService;

    private final DailyReportLlmClient dailyReportLlmClient;

    public DailyReportResponse generateDailyReport(Long userId, DailyReportRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. id: " + userId));

        DailyQuestion question = dailyQuestionRepository.findById(request.questionId())
                .orElseThrow(() -> new NotFoundException("질문을 찾을 수 없습니다. id: " + request.questionId()));

        PrepareDailyResultDto prep = dailyReportTxService.prepareDaily(user, question, request.answer());

        AiReportResultDto dto;
        try {
            dto = dailyReportLlmClient.generate(question.getQuestionText(), request.answer());
        } catch (Exception e) {
            dailyReportTxService.failDaily(prep.reportId());
            throw e;
        }

        long balanceAfter = dailyReportTxService.confirmDailyAndReward(prep, dto);

        return new DailyReportResponse(
                prep.reportId(),
                dto.message(),
                dto.emotion(),
                balanceAfter
        );
    }
}
