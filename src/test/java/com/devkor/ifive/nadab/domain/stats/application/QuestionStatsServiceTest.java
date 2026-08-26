package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionListItemViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewQuery;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewRowViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewSort;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewSortDirection;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewViewModel;
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

    @Test
    void overview_defaults_to_current_exposure_descending_with_stable_id_tiebreaker() {
        QuestionStatsRepository repository = mock(QuestionStatsRepository.class);
        QuestionStatsService service = new QuestionStatsService(repository);
        DailyQuestionOverviewRowViewModel question1 = overviewQuestion(
                1L, InterestCode.PREFERENCE, "질문 1", 1, true,
                2L, 1L, 0L, 1L,
                7L, 3L, 2L, 2L
        );
        DailyQuestionOverviewRowViewModel question2 = overviewQuestion(
                2L, InterestCode.EMOTION, "질문 2", 2, true,
                5L, 2L, 2L, 1L,
                5L, 2L, 2L, 1L
        );
        DailyQuestionOverviewRowViewModel question3 = overviewQuestion(
                3L, InterestCode.RELATIONSHIP, "질문 3", 2, false,
                5L, 1L, 3L, 1L,
                10L, 4L, 4L, 2L
        );
        when(repository.findQuestionOverview()).thenReturn(List.of(question1, question3, question2));

        DailyQuestionOverviewViewModel overview = service.getQuestionOverview(
                DailyQuestionOverviewQuery.defaults()
        );

        assertThat(overview.rows())
                .extracting(DailyQuestionOverviewRowViewModel::questionId)
                .containsExactly(2L, 3L, 1L);
        assertThat(overview.totalQuestionCount()).isEqualTo(3);
        assertThat(overview.filteredQuestionCount()).isEqualTo(3);
        assertThat(overview.rows().getLast().totalExposureCount()).isEqualTo(7L);
        assertThat(overview.refreshedAt()).isNotBlank();
    }

    @Test
    void overview_filters_by_keyword_metadata_and_minimum_current_exposure() {
        QuestionStatsRepository repository = mock(QuestionStatsRepository.class);
        QuestionStatsService service = new QuestionStatsService(repository);
        DailyQuestionOverviewRowViewModel matchingQuestion = overviewQuestion(
                12L, InterestCode.EMOTION, "비 올 때 듣는 노래", 2, false,
                8L, 3L, 4L, 1L,
                12L, 5L, 5L, 2L
        );
        when(repository.findQuestionOverview()).thenReturn(List.of(
                overviewQuestion(
                        1L, InterestCode.PREFERENCE, "자주 듣는 노래", 1, true,
                        10L, 6L, 2L, 2L,
                        10L, 6L, 2L, 2L
                ),
                matchingQuestion,
                overviewQuestion(
                        13L, InterestCode.EMOTION, "비 오는 날의 질문", 2, false,
                        2L, 1L, 1L, 0L,
                        2L, 1L, 1L, 0L
                )
        ));
        DailyQuestionOverviewQuery query = new DailyQuestionOverviewQuery(
                "  비  ",
                InterestCode.EMOTION,
                2,
                false,
                5L,
                DailyQuestionOverviewSort.QUESTION_ID,
                DailyQuestionOverviewSortDirection.ASC
        );

        DailyQuestionOverviewViewModel overview = service.getQuestionOverview(query);

        assertThat(overview.rows()).containsExactly(matchingQuestion);
        assertThat(overview.totalQuestionCount()).isEqualTo(3);
        assertThat(overview.filteredQuestionCount()).isEqualTo(1);
        assertThat(overview.query().keyword()).isEqualTo("비");
    }

    @Test
    void overview_can_rank_reroll_rate_after_excluding_small_samples() {
        QuestionStatsRepository repository = mock(QuestionStatsRepository.class);
        QuestionStatsService service = new QuestionStatsService(repository);
        DailyQuestionOverviewRowViewModel question1 = overviewQuestion(
                1L, InterestCode.PREFERENCE, "질문 1", 1, true,
                100L, 70L, 20L, 10L,
                100L, 70L, 20L, 10L
        );
        DailyQuestionOverviewRowViewModel question2 = overviewQuestion(
                2L, InterestCode.PREFERENCE, "질문 2", 1, true,
                10L, 4L, 5L, 1L,
                10L, 4L, 5L, 1L
        );
        DailyQuestionOverviewRowViewModel smallSample = overviewQuestion(
                3L, InterestCode.PREFERENCE, "질문 3", 1, true,
                2L, 0L, 2L, 0L,
                2L, 0L, 2L, 0L
        );
        when(repository.findQuestionOverview()).thenReturn(List.of(question1, question2, smallSample));
        DailyQuestionOverviewQuery query = new DailyQuestionOverviewQuery(
                "",
                null,
                null,
                null,
                10L,
                DailyQuestionOverviewSort.CURRENT_REROLL_RATE,
                DailyQuestionOverviewSortDirection.DESC
        );

        DailyQuestionOverviewViewModel overview = service.getQuestionOverview(query);

        assertThat(overview.rows())
                .extracting(DailyQuestionOverviewRowViewModel::questionId)
                .containsExactly(2L, 1L);
        assertThat(overview.rows().getFirst().currentRerollRate()).isEqualTo(0.5);
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

    private DailyQuestionOverviewRowViewModel overviewQuestion(
            long questionId,
            InterestCode interestCode,
            String questionText,
            int questionLevel,
            boolean active,
            long currentExposureCount,
            long currentAnsweredCount,
            long currentRerolledCount,
            long currentUnansweredCount,
            long totalExposureCount,
            long totalAnsweredCount,
            long totalRerolledCount,
            long totalUnansweredCount
    ) {
        return new DailyQuestionOverviewRowViewModel(
                questionId,
                interestCode,
                questionText,
                questionLevel,
                2,
                active ? null : EFFECTIVE_FROM,
                EFFECTIVE_FROM,
                currentExposureCount,
                currentAnsweredCount,
                currentRerolledCount,
                currentUnansweredCount,
                totalExposureCount,
                totalAnsweredCount,
                totalRerolledCount,
                totalUnansweredCount
        );
    }
}
