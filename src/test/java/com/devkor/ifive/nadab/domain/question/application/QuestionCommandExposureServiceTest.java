package com.devkor.ifive.nadab.domain.question.application;

import com.devkor.ifive.nadab.domain.dailyreport.core.repository.AnswerEntryRepository;
import com.devkor.ifive.nadab.domain.question.application.helper.DailyQuestionSelector;
import com.devkor.ifive.nadab.domain.question.application.helper.QuestionLevelPolicy;
import com.devkor.ifive.nadab.domain.question.api.dto.response.DailyQuestionResponse;
import com.devkor.ifive.nadab.domain.question.api.dto.response.DailyQuestionResponseV2;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestionRevision;
import com.devkor.ifive.nadab.domain.question.core.entity.UserDailyQuestion;
import com.devkor.ifive.nadab.domain.question.core.repository.UserDailyQuestionRepository;
import com.devkor.ifive.nadab.domain.question.core.service.DailyQuestionExposureService;
import com.devkor.ifive.nadab.domain.user.core.entity.Interest;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserInterestRepository;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionCommandExposureServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long INTEREST_ID = 2L;
    private static final LocalDate ASSIGNMENT_DATE = LocalDate.of(2026, 8, 26);

    @Mock
    UserRepository userRepository;

    @Mock
    UserDailyQuestionRepository userDailyQuestionRepository;

    @Mock
    UserInterestRepository userInterestRepository;

    @Mock
    AnswerEntryRepository answerEntryRepository;

    @Mock
    QuestionLevelPolicy questionLevelPolicy;

    @Mock
    DailyQuestionSelector dailyQuestionSelector;

    @Mock
    DailyQuestionExposureService exposureService;

    QuestionCommandService service;
    QuestionCommandServiceV2 serviceV2;

    @BeforeEach
    void setUp() {
        service = new QuestionCommandService(
                userRepository,
                userDailyQuestionRepository,
                userInterestRepository,
                answerEntryRepository,
                questionLevelPolicy,
                dailyQuestionSelector,
                exposureService
        );
        serviceV2 = new QuestionCommandServiceV2(
                userRepository,
                userDailyQuestionRepository,
                userInterestRepository,
                answerEntryRepository,
                questionLevelPolicy,
                dailyQuestionSelector,
                exposureService
        );
    }

    @Test
    void v1_create_records_initial_assignment_exposure() {
        User user = user();
        DailyQuestion question = mock(DailyQuestion.class);
        stubInitialSelection(user);
        when(questionLevelPolicy.levelOnlyFor(eq(user), any(OffsetDateTime.class))).thenReturn(1);
        when(dailyQuestionSelector.pickFirst(USER_ID, INTEREST_ID, 1)).thenReturn(question);

        UserDailyQuestion assignment = service.createTodayQuestion(USER_ID, ASSIGNMENT_DATE);

        assertThat(assignment.getDailyQuestion()).isEqualTo(question);
        verify(exposureService).recordInitialAssignment(assignment);
    }

    @Test
    void v2_create_records_initial_assignment_exposure() {
        User user = user();
        DailyQuestion question = mock(DailyQuestion.class);
        stubInitialSelection(user);
        when(userDailyQuestionRepository.existsByUserId(USER_ID)).thenReturn(false);
        when(questionLevelPolicy.levelOnlyForFirstTime(true)).thenReturn(1);
        when(dailyQuestionSelector.pickFirst(USER_ID, INTEREST_ID, 1)).thenReturn(question);

        UserDailyQuestion assignment = serviceV2.createTodayQuestion(USER_ID, ASSIGNMENT_DATE);

        assertThat(assignment.getDailyQuestion()).isEqualTo(question);
        verify(exposureService).recordInitialAssignment(assignment);
    }

    @Test
    void v1_reroll_records_exposure_after_assignment_change() {
        User user = user();
        DailyQuestion currentQuestion = questionWithId(10L);
        DailyQuestion newQuestion = responseQuestion(20L);
        UserDailyQuestion assignment = mock(UserDailyQuestion.class);
        when(assignment.isRerollUsed()).thenReturn(false);
        when(assignment.getUser()).thenReturn(user);
        when(assignment.getDailyQuestion()).thenReturn(currentQuestion);
        when(userDailyQuestionRepository.findByUserIdAndDate(eq(USER_ID), any(LocalDate.class)))
                .thenReturn(Optional.of(assignment));
        when(answerEntryRepository.existsByUserAndDate(eq(user), any(LocalDate.class))).thenReturn(false);
        when(userInterestRepository.findInterestIdByUserId(USER_ID)).thenReturn(Optional.of(INTEREST_ID));
        when(questionLevelPolicy.levelOnlyFor(eq(user), any(OffsetDateTime.class))).thenReturn(1);
        when(dailyQuestionSelector.pickReroll(USER_ID, INTEREST_ID, 10L, 1)).thenReturn(newQuestion);

        service.rerollTodayQuestion(USER_ID);

        InOrder inOrder = inOrder(assignment, exposureService);
        inOrder.verify(assignment).rerollTo(newQuestion);
        inOrder.verify(exposureService).recordReroll(assignment, newQuestion);
    }

    @Test
    void v2_reroll_records_exposure_after_assignment_change() {
        User user = user();
        DailyQuestion currentQuestion = questionWithId(10L);
        DailyQuestion newQuestion = responseQuestion(20L);
        UserDailyQuestion assignment = mock(UserDailyQuestion.class);
        when(assignment.getRerollLeft()).thenReturn(5, 4);
        when(assignment.getUser()).thenReturn(user);
        when(assignment.getDailyQuestion()).thenReturn(currentQuestion);
        when(userDailyQuestionRepository.findByUserIdAndDate(eq(USER_ID), any(LocalDate.class)))
                .thenReturn(Optional.of(assignment));
        when(answerEntryRepository.existsByUserAndDate(eq(user), any(LocalDate.class))).thenReturn(false);
        when(userInterestRepository.findInterestIdByUserId(USER_ID)).thenReturn(Optional.of(INTEREST_ID));
        when(dailyQuestionSelector.pickReroll(USER_ID, INTEREST_ID, 10L, null)).thenReturn(newQuestion);

        serviceV2.rerollTodayQuestion(USER_ID);

        InOrder inOrder = inOrder(assignment, exposureService);
        inOrder.verify(assignment).rerollTo(newQuestion);
        inOrder.verify(exposureService).recordReroll(assignment, newQuestion);
    }

    @Test
    void v1_get_returns_revision_snapshot_for_tracked_assignment() {
        UserDailyQuestion assignment = mock(UserDailyQuestion.class);
        DailyQuestion question = questionWithId(10L);
        DailyQuestionRevision revision = revisionSnapshot();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
        when(userDailyQuestionRepository.findByUserIdAndDate(eq(USER_ID), any(LocalDate.class)))
                .thenReturn(Optional.of(assignment));
        when(assignment.getDailyQuestion()).thenReturn(question);
        when(assignment.isRerollUsed()).thenReturn(false);
        when(exposureService.findLatestRevision(assignment)).thenReturn(Optional.of(revision));
        when(answerEntryRepository.existsActiveAnswer(USER_ID, 10L)).thenReturn(false);

        DailyQuestionResponse response = service.getOrCreateTodayQuestion(USER_ID);

        assertThat(response.questionText()).isEqualTo("revision question");
        assertThat(response.empathyGuide()).isEqualTo("revision empathy");
        assertThat(response.hintGuide()).isEqualTo("revision hint");
        assertThat(response.leadingQuestionGuide()).isEqualTo("revision leading");
        assertThat(response.interestCode()).isEqualTo(InterestCode.PREFERENCE.toString());
    }

    @Test
    void v2_get_returns_revision_snapshot_for_tracked_assignment() {
        UserDailyQuestion assignment = mock(UserDailyQuestion.class);
        DailyQuestion question = questionWithId(10L);
        DailyQuestionRevision revision = revisionSnapshot();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
        when(userDailyQuestionRepository.findByUserIdAndDate(eq(USER_ID), any(LocalDate.class)))
                .thenReturn(Optional.of(assignment));
        when(assignment.getDailyQuestion()).thenReturn(question);
        when(assignment.getRerollLeft()).thenReturn(4);
        when(exposureService.findLatestRevision(assignment)).thenReturn(Optional.of(revision));
        when(answerEntryRepository.existsActiveAnswer(USER_ID, 10L)).thenReturn(false);

        DailyQuestionResponseV2 response = serviceV2.getOrCreateTodayQuestion(USER_ID);

        assertThat(response.questionText()).isEqualTo("revision question");
        assertThat(response.empathyGuide()).isEqualTo("revision empathy");
        assertThat(response.hintGuide()).isEqualTo("revision hint");
        assertThat(response.leadingQuestionGuide()).isEqualTo("revision leading");
        assertThat(response.interestCode()).isEqualTo(InterestCode.PREFERENCE.toString());
    }

    private void stubInitialSelection(User user) {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userInterestRepository.findInterestIdByUserId(USER_ID)).thenReturn(Optional.of(INTEREST_ID));
        when(userDailyQuestionRepository.save(any(UserDailyQuestion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private User user() {
        User user = User.createUser("test@test.com", "hashed_password");
        ReflectionTestUtils.setField(user, "id", USER_ID);
        return user;
    }

    private DailyQuestion questionWithId(Long id) {
        DailyQuestion question = mock(DailyQuestion.class);
        when(question.getId()).thenReturn(id);
        return question;
    }

    private DailyQuestion responseQuestion(Long id) {
        Interest interest = mock(Interest.class);
        when(interest.getCode()).thenReturn(InterestCode.PREFERENCE);
        DailyQuestion question = mock(DailyQuestion.class);
        when(question.getId()).thenReturn(id);
        when(question.getInterest()).thenReturn(interest);
        when(question.getQuestionText()).thenReturn("question");
        return question;
    }

    private DailyQuestionRevision revisionSnapshot() {
        Interest interest = mock(Interest.class);
        when(interest.getCode()).thenReturn(InterestCode.PREFERENCE);
        DailyQuestionRevision revision = mock(DailyQuestionRevision.class);
        when(revision.getInterest()).thenReturn(interest);
        when(revision.getQuestionText()).thenReturn("revision question");
        when(revision.getEmpathyGuide()).thenReturn("revision empathy");
        when(revision.getHintGuide()).thenReturn("revision hint");
        when(revision.getLeadingQuestionGuide()).thenReturn("revision leading");
        return revision;
    }
}
