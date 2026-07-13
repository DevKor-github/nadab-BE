package com.devkor.ifive.nadab.domain.dailyreport.application;

import com.devkor.ifive.nadab.domain.dailyreport.application.event.DailyReportCompletedEvent;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.AiDailyReportResultDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.dto.PrepareDailyResultDto;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.Emotion;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionName;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.DailyReportRepository;
import com.devkor.ifive.nadab.domain.dailyreport.core.repository.EmotionRepository;
import com.devkor.ifive.nadab.domain.dailyreport.core.service.AnswerEntryService;
import com.devkor.ifive.nadab.domain.dailyreport.core.service.PendingDailyReportService;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.user.core.entity.Interest;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.wallet.core.entity.UserWallet;
import com.devkor.ifive.nadab.domain.wallet.core.repository.CrystalLogRepository;
import com.devkor.ifive.nadab.domain.wallet.core.repository.UserWalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyReportTxServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DailyReportRepository dailyReportRepository;

    @Mock
    private EmotionRepository emotionRepository;

    @Mock
    private UserWalletRepository userWalletRepository;

    @Mock
    private CrystalLogRepository crystalLogRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AnswerEntryService answerEntryService;

    @Mock
    private PendingDailyReportService pendingDailyReportService;

    private DailyReportTxService service;

    @BeforeEach
    void setUp() {
        service = new DailyReportTxService(
                userRepository,
                dailyReportRepository,
                emotionRepository,
                userWalletRepository,
                crystalLogRepository,
                eventPublisher,
                answerEntryService,
                pendingDailyReportService
        );
    }

    @Test
    void confirmDailyAndReward_publishes_completed_event_with_answer_and_report_ids() {
        Long userId = 1L;
        Long answerEntryId = 30L;
        Long reportId = 100L;
        User user = User.createUser("test@test.com", "hashed_password");
        ReflectionTestUtils.setField(user, "id", userId);
        AnswerEntry answerEntry = answerEntry(answerEntryId);
        EmotionName emotionName = EmotionName.values()[0];
        Emotion emotion = mock(Emotion.class);
        UserWallet wallet = UserWallet.create(user, 110L);

        when(emotion.getId()).thenReturn(5L);
        when(emotionRepository.findByName(emotionName)).thenReturn(Optional.of(emotion));
        when(userWalletRepository.charge(userId, 10L)).thenReturn(1);
        when(userWalletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(crystalLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.confirmDailyAndReward(
                new PrepareDailyResultDto(answerEntry, reportId, userId),
                new AiDailyReportResultDto("report", emotionName.name()),
                null
        );

        verify(dailyReportRepository).markCompleted(reportId, "report", 5L);
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOfSatisfying(DailyReportCompletedEvent.class, event -> {
            assertThat(event.getUserId()).isEqualTo(userId);
            assertThat(event.getAnswerEntryId()).isEqualTo(answerEntryId);
            assertThat(event.getReportId()).isEqualTo(reportId);
            assertThat(event.getInterestCode()).isEqualTo(InterestCode.RELATIONSHIP);
        });
    }

    private AnswerEntry answerEntry(Long id) {
        Interest interest = mock(Interest.class);
        when(interest.getCode()).thenReturn(InterestCode.RELATIONSHIP);
        DailyQuestion question = mock(DailyQuestion.class);
        when(question.getInterest()).thenReturn(interest);
        AnswerEntry answerEntry = mock(AnswerEntry.class);
        when(answerEntry.getId()).thenReturn(id);
        when(answerEntry.getQuestion()).thenReturn(question);
        return answerEntry;
    }
}
