package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionListItemViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewQuery;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewRowViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewSortDirection;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionReactionStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionRevisionStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionStatsViewModel;
import com.devkor.ifive.nadab.domain.stats.core.repository.QuestionStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionStatsService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final QuestionStatsRepository repository;

    public DailyQuestionStatsViewModel getQuestionStats(@Nullable Long questionId) {
        List<DailyQuestionListItemViewModel> questions = repository.findQuestions();
        DailyQuestionListItemViewModel selectedQuestion = selectQuestion(questions, questionId);
        List<DailyQuestionRevisionStatsViewModel> revisions = selectedQuestion == null
                ? List.of()
                : repository.findRevisionStats(selectedQuestion.questionId());

        long exposureCount = revisions.stream()
                .mapToLong(DailyQuestionRevisionStatsViewModel::exposureCount)
                .sum();
        long answeredCount = revisions.stream()
                .mapToLong(DailyQuestionRevisionStatsViewModel::answeredCount)
                .sum();
        long rerolledCount = revisions.stream()
                .mapToLong(DailyQuestionRevisionStatsViewModel::rerolledCount)
                .sum();
        long unansweredCount = revisions.stream()
                .mapToLong(DailyQuestionRevisionStatsViewModel::unansweredCount)
                .sum();

        return new DailyQuestionStatsViewModel(
                questions,
                selectedQuestion,
                new DailyQuestionReactionStatsViewModel(
                        exposureCount,
                        answeredCount,
                        rerolledCount,
                        unansweredCount
                ),
                revisions,
                refreshedAt()
        );
    }

    public DailyQuestionOverviewViewModel getQuestionOverview(DailyQuestionOverviewQuery query) {
        List<DailyQuestionOverviewRowViewModel> allQuestions = repository.findQuestionOverview();
        String normalizedKeyword = query.keyword().toLowerCase(Locale.ROOT);

        List<DailyQuestionOverviewRowViewModel> filteredQuestions = allQuestions.stream()
                .filter(question -> matchesKeyword(question, normalizedKeyword))
                .filter(question -> query.interestCode() == null
                        || question.interestCode() == query.interestCode())
                .filter(question -> query.questionLevel() == null
                        || question.questionLevel() == query.questionLevel())
                .filter(question -> query.active() == null
                        || question.active() == query.active())
                .filter(question -> question.currentExposureCount() >= query.minimumCurrentExposureCount())
                .sorted(overviewComparator(query))
                .toList();

        return new DailyQuestionOverviewViewModel(
                filteredQuestions,
                allQuestions.size(),
                query,
                refreshedAt()
        );
    }

    @Nullable
    private DailyQuestionListItemViewModel selectQuestion(
            List<DailyQuestionListItemViewModel> questions,
            @Nullable Long questionId
    ) {
        if (questionId == null) {
            return questions.isEmpty() ? null : questions.getFirst();
        }

        return questions.stream()
                .filter(question -> question.questionId() == questionId)
                .findFirst()
                .orElse(null);
    }

    private boolean matchesKeyword(
            DailyQuestionOverviewRowViewModel question,
            String normalizedKeyword
    ) {
        return normalizedKeyword.isEmpty()
                || Long.toString(question.questionId()).contains(normalizedKeyword)
                || question.questionText().toLowerCase(Locale.ROOT).contains(normalizedKeyword);
    }

    private Comparator<DailyQuestionOverviewRowViewModel> overviewComparator(
            DailyQuestionOverviewQuery query
    ) {
        Comparator<DailyQuestionOverviewRowViewModel> comparator = switch (query.sort()) {
            case QUESTION_ID -> Comparator.comparingLong(DailyQuestionOverviewRowViewModel::questionId);
            case CURRENT_REVISION_NO -> Comparator.comparingInt(DailyQuestionOverviewRowViewModel::currentRevisionNo);
            case CURRENT_EXPOSURE_COUNT -> Comparator.comparingLong(DailyQuestionOverviewRowViewModel::currentExposureCount);
            case CURRENT_ANSWERED_COUNT -> Comparator.comparingLong(DailyQuestionOverviewRowViewModel::currentAnsweredCount);
            case CURRENT_ANSWER_RATE -> Comparator.comparingDouble(DailyQuestionOverviewRowViewModel::currentAnswerRate);
            case CURRENT_REROLLED_COUNT -> Comparator.comparingLong(DailyQuestionOverviewRowViewModel::currentRerolledCount);
            case CURRENT_REROLL_RATE -> Comparator.comparingDouble(DailyQuestionOverviewRowViewModel::currentRerollRate);
            case CURRENT_UNANSWERED_COUNT -> Comparator.comparingLong(DailyQuestionOverviewRowViewModel::currentUnansweredCount);
            case TOTAL_EXPOSURE_COUNT -> Comparator.comparingLong(DailyQuestionOverviewRowViewModel::totalExposureCount);
            case TOTAL_ANSWERED_COUNT -> Comparator.comparingLong(DailyQuestionOverviewRowViewModel::totalAnsweredCount);
            case TOTAL_ANSWER_RATE -> Comparator.comparingDouble(DailyQuestionOverviewRowViewModel::totalAnswerRate);
        };

        if (query.direction() == DailyQuestionOverviewSortDirection.DESC) {
            comparator = comparator.reversed();
        }

        return comparator
                .thenComparing(Comparator.comparingLong(
                        DailyQuestionOverviewRowViewModel::currentExposureCount
                ).reversed())
                .thenComparingLong(DailyQuestionOverviewRowViewModel::questionId);
    }

    private String refreshedAt() {
        return OffsetDateTime.now(SEOUL).format(FMT);
    }
}
