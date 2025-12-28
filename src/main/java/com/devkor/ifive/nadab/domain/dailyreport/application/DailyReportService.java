package com.devkor.ifive.nadab.domain.dailyreport.application;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.request.DailyReportRequest;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.CreateDailyReportResponse;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.PrepareDailyResultDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.AiReportResultDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.Emotion;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionName;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.AnswerEntryRepository;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.EmotionRepository;
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
    private final AnswerEntryRepository answerEntryRepository;
    private final EmotionRepository emotionRepository;

    private final DailyReportTxService dailyReportTxService;

    private final DailyReportLlmClient dailyReportLlmClient;

    public CreateDailyReportResponse generateDailyReport(Long userId, DailyReportRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. id: " + userId));

        DailyQuestion question = dailyQuestionRepository.findById(request.questionId())
                .orElseThrow(() -> new NotFoundException("질문을 찾을 수 없습니다. id: " + request.questionId()));

        PrepareDailyResultDto prep = dailyReportTxService.prepareDaily(user, question, request.answer());

        AnswerEntry answerEntry = answerEntryRepository.findById(prep.entryId())
                .orElseThrow(() -> new NotFoundException("답변 엔트리를 찾을 수 없습니다. id: " + prep.entryId()));

        AiReportResultDto dto;
        try {
            dto = dailyReportLlmClient.generate(question.getQuestionText(), answerEntry.getContent());
        } catch (Exception e) {
            dailyReportTxService.failDaily(prep.reportId());
            throw e;
        }

        long balanceAfter = dailyReportTxService.confirmDailyAndReward(prep, dto);

        Emotion emotion = emotionRepository.findByName(EmotionName.valueOf(dto.emotion()))
                .orElseThrow(() -> new NotFoundException("감정 코드를 찾을 수 없습니다: " + dto.emotion()));

        return new CreateDailyReportResponse(
                prep.reportId(),
                dto.message(),
                emotion.getCode().toString(),
                balanceAfter
        );
    }
}
