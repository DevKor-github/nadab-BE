package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionListItemViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionRevisionStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.repository.QuestionStatsRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuestionStatsServiceTest {

    private static final OffsetDateTime EFFECTIVE_FROM =
            OffsetDateTime.parse("2026-08-26T00:00:00+09:00");

    @Test
    void defaults_to_first_question_and_sums_all_revision_counts() {
        QuestionStatsRepository repository = mock(QuestionStatsRepository.class);
        QuestionStatsService service = new QuestionStatsService(repository);
        DailyQuestionListItemViewModel firstQuestion = question(1L);
        List<DailyQuestionListItemViewModel> questions = List.of(firstQuestion, question(2L));
        List<DailyQuestionRevisionStatsViewModel> revisions = List.of(
                revision(2, 2L, 1L, 0L, 1L),
                revision(1, 3L, 1L, 1L, 1L)
        );
        when(repository.findQuestions()).thenReturn(questions);
        when(repository.findRevisionStats(1L)).thenReturn(revisions);

        DailyQuestionStatsViewModel stats = service.getQuestionStats(null);

        assertThat(stats.selectedQuestion()).isEqualTo(firstQuestion);
        assertThat(stats.revisions()).isEqualTo(revisions);
        assertThat(stats.total().exposureCount()).isEqualTo(5L);
        assertThat(stats.total().answeredCount()).isEqualTo(2L);
        assertThat(stats.total().rerolledCount()).isEqualTo(1L);
        assertThat(stats.total().unansweredCount()).isEqualTo(2L);
        assertThat(stats.total().answerRate()).isEqualTo(0.4);
        assertThat(stats.refreshedAt()).isNotBlank();
        verify(repository).findRevisionStats(1L);
    }

    @Test
    void selects_requested_question() {
        QuestionStatsRepository repository = mock(QuestionStatsRepository.class);
        QuestionStatsService service = new QuestionStatsService(repository);
        DailyQuestionListItemViewModel selectedQuestion = question(2L);
        when(repository.findQuestions()).thenReturn(List.of(question(1L), selectedQuestion));
        when(repository.findRevisionStats(2L)).thenReturn(List.of(revision(1, 1L, 1L, 0L, 0L)));

        DailyQuestionStatsViewModel stats = service.getQuestionStats(2L);

        assertThat(stats.selectedQuestion()).isEqualTo(selectedQuestion);
        assertThat(stats.total().answerRate()).isEqualTo(1.0);
        verify(repository).findRevisionStats(2L);
    }

    @Test
    void returns_empty_detail_when_requested_question_does_not_exist() {
        QuestionStatsRepository repository = mock(QuestionStatsRepository.class);
        QuestionStatsService service = new QuestionStatsService(repository);
        when(repository.findQuestions()).thenReturn(List.of(question(1L)));

        DailyQuestionStatsViewModel stats = service.getQuestionStats(999L);

        assertThat(stats.selectedQuestion()).isNull();
        assertThat(stats.revisions()).isEmpty();
        assertThat(stats.total().exposureCount()).isZero();
        verify(repository, never()).findRevisionStats(999L);
    }

    private DailyQuestionListItemViewModel question(long questionId) {
        return new DailyQuestionListItemViewModel(
                questionId,
                InterestCode.PREFERENCE,
                "질문 " + questionId,
                1,
                1,
                null
        );
    }

    private DailyQuestionRevisionStatsViewModel revision(
            int revisionNo,
            long exposureCount,
            long answeredCount,
            long rerolledCount,
            long unansweredCount
    ) {
        return new DailyQuestionRevisionStatsViewModel(
                (long) revisionNo,
                revisionNo,
                InterestCode.PREFERENCE,
                "revision " + revisionNo,
                1,
                null,
                null,
                null,
                null,
                EFFECTIVE_FROM,
                "V_TEST_" + revisionNo,
                exposureCount,
                answeredCount,
                rerolledCount,
                unansweredCount
        );
    }
}
