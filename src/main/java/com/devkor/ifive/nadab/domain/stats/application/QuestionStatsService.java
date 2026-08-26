package com.devkor.ifive.nadab.domain.stats.application;

import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionListItemViewModel;
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
import java.util.List;

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
                OffsetDateTime.now(SEOUL).format(FMT)
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
}
