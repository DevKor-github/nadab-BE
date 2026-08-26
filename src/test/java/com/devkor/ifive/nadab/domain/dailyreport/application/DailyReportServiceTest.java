package com.devkor.ifive.nadab.domain.dailyreport.application;

import com.devkor.ifive.nadab.domain.dailyreport.api.dto.request.DailyReportRequest;
import com.devkor.ifive.nadab.domain.dailyreport.api.dto.response.CreateDailyReportResponse;
import com.devkor.ifive.nadab.domain.dailyreport.application.helper.DailyReportModelSelector;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.AiDailyReportResultDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.ConfirmDailyAndRewardDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.PrepareDailyResultDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.Emotion;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionCode;
import com.devkor.ifive.nadab.domain.dailyreport.core.properties.DailyReportLlmProperties.ModelCandidate;
import com.devkor.ifive.nadab.domain.dailyreport.infra.DailyReportLlmClient;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.entity.UserDailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.repository.DailyQuestionRepository;
import com.devkor.ifive.nadab.domain.question.core.repository.UserDailyQuestionRepository;
import com.devkor.ifive.nadab.domain.reportlog.application.ReportGenerationLogRecorder;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationStep;
import com.devkor.ifive.nadab.domain.reportlog.core.entity.ReportGenerationType;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.user.core.service.ProfileImageService;
import com.devkor.ifive.nadab.domain.user.infra.ProfileImageUrlBuilder;
import com.devkor.ifive.nadab.global.infra.llm.LlmGenerationResult;
import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.global.infra.llm.LlmTokenUsage;
import com.devkor.ifive.nadab.global.shared.util.TodayDateTimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyReportServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    DailyQuestionRepository dailyQuestionRepository;

    @Mock
    UserDailyQuestionRepository userDailyQuestionRepository;

    @Mock
    DailyReportTxService dailyReportTxService;

    @Mock
    ProfileImageService profileImageService;

    @Mock
    DailyReportModelSelector dailyReportModelSelector;

    @Mock
    DailyReportLlmClient dailyReportLlmClient;

    @Mock
    ReportGenerationLogRecorder reportGenerationLogRecorder;

    @Mock
    ProfileImageUrlBuilder profileImageUrlBuilder;

    DailyReportService dailyReportService;

    @BeforeEach
    void setUp() {
        dailyReportService = new DailyReportService(
                userRepository,
                dailyQuestionRepository,
                userDailyQuestionRepository,
                dailyReportTxService,
                profileImageService,
                dailyReportModelSelector,
                dailyReportLlmClient,
                reportGenerationLogRecorder,
                profileImageUrlBuilder
        );
        ReflectionTestUtils.setField(dailyReportService, "env", "test");
    }

    @Test
    void generate_daily_report_records_token_usage_before_succeeding_generation_log() {
        // given
        Long userId = 1L;
        Long reportId = 100L;
        Long generationLogId = 10L;
        User user = user(userId);
        DailyQuestion question = dailyQuestion(20L);
        LocalDate today = TodayDateTimeProvider.getTodayDate();
        AnswerEntry answerEntry = AnswerEntry.create(user, question, "answer", today, null);
        AiDailyReportResultDto aiResult = new AiDailyReportResultDto("message", "ACHIEVEMENT");
        Emotion emotion = emotion(EmotionCode.ACHIEVEMENT);
        ModelCandidate modelCandidate = modelCandidate("gpt-5.6-luna");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(dailyQuestionRepository.findByIdWithInterest(20L)).thenReturn(Optional.of(question));
        UserDailyQuestion assignment = UserDailyQuestion.create(user, today, question);
        when(userDailyQuestionRepository.findByUserIdAndDate(eq(userId), any(LocalDate.class)))
                .thenReturn(Optional.of(assignment));
        when(dailyReportTxService.prepareDaily(user, question, assignment, "answer", false, null))
                .thenReturn(new PrepareDailyResultDto(answerEntry, reportId, userId, "revision question"));
        when(dailyReportModelSelector.select()).thenReturn(modelCandidate);
        when(reportGenerationLogRecorder.start(
                userId,
                ReportGenerationType.DAILY,
                reportId,
                ReportGenerationStep.DAILY_GENERATE,
                LlmProvider.OPENAI,
                "gpt-5.6-luna"
        )).thenReturn(generationLogId);
        when(dailyReportLlmClient.generate("revision question", answerEntry, modelCandidate))
                .thenReturn(new LlmGenerationResult<>(aiResult, new LlmTokenUsage(100L, 50L, 150L)));
        when(dailyReportTxService.confirmDailyAndReward(
                any(PrepareDailyResultDto.class),
                eq(aiResult),
                eq(null)
        )).thenReturn(new ConfirmDailyAndRewardDto(emotion, 110L));

        // when
        CreateDailyReportResponse response = dailyReportService.generateDailyReport(
                userId,
                new DailyReportRequest(20L, "answer", null, null)
        );

        // then
        assertThat(response.reportId()).isEqualTo(reportId);
        assertThat(response.content()).isEqualTo("message");
        assertThat(response.balanceAfter()).isEqualTo(110L);
        verify(dailyReportModelSelector).select();
        verify(dailyReportLlmClient).generate("revision question", answerEntry, modelCandidate);

        InOrder inOrder = inOrder(reportGenerationLogRecorder);
        inOrder.verify(reportGenerationLogRecorder).recordTokenUsage(generationLogId, 100L, 50L, 150L, null);
        inOrder.verify(reportGenerationLogRecorder).succeed(generationLogId);
    }

    @Test
    void generate_daily_report_records_null_token_usage_when_usage_is_empty() {
        // given
        Long userId = 1L;
        Long reportId = 100L;
        Long generationLogId = 10L;
        User user = user(userId);
        DailyQuestion question = dailyQuestion(20L);
        LocalDate today = TodayDateTimeProvider.getTodayDate();
        AnswerEntry answerEntry = AnswerEntry.create(user, question, "answer", today, null);
        AiDailyReportResultDto aiResult = new AiDailyReportResultDto("message", "ACHIEVEMENT");
        Emotion emotion = emotion(EmotionCode.ACHIEVEMENT);
        ModelCandidate modelCandidate = modelCandidate("gpt-4o-mini");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(dailyQuestionRepository.findByIdWithInterest(20L)).thenReturn(Optional.of(question));
        UserDailyQuestion assignment = UserDailyQuestion.create(user, today, question);
        when(userDailyQuestionRepository.findByUserIdAndDate(eq(userId), any(LocalDate.class)))
                .thenReturn(Optional.of(assignment));
        when(dailyReportTxService.prepareDaily(user, question, assignment, "answer", false, null))
                .thenReturn(new PrepareDailyResultDto(answerEntry, reportId, userId, "question"));
        when(dailyReportModelSelector.select()).thenReturn(modelCandidate);
        when(reportGenerationLogRecorder.start(any(), any(), any(), any(), any(), any()))
                .thenReturn(generationLogId);
        when(dailyReportLlmClient.generate("question", answerEntry, modelCandidate))
                .thenReturn(new LlmGenerationResult<>(aiResult, LlmTokenUsage.empty()));
        when(dailyReportTxService.confirmDailyAndReward(any(), eq(aiResult), eq(null)))
                .thenReturn(new ConfirmDailyAndRewardDto(emotion, 110L));

        // when
        dailyReportService.generateDailyReport(userId, new DailyReportRequest(20L, "answer", null, null));

        // then
        verify(reportGenerationLogRecorder).recordTokenUsage(generationLogId, null, null, null, null);
    }

    private User user(Long id) {
        User user = User.createUser("test@test.com", "hashed_password");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private DailyQuestion dailyQuestion(Long id) {
        DailyQuestion question = mock(DailyQuestion.class);
        when(question.getId()).thenReturn(id);
        return question;
    }

    private Emotion emotion(EmotionCode code) {
        Emotion emotion = mock(Emotion.class);
        when(emotion.getCode()).thenReturn(code);
        return emotion;
    }

    private ModelCandidate modelCandidate(String model) {
        ModelCandidate modelCandidate = new ModelCandidate();
        modelCandidate.setModel(model);
        return modelCandidate;
    }
}
