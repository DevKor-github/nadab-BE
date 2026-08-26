package com.devkor.ifive.nadab.domain.stats.core.repository;

import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionListItemViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionRevisionStatsViewModel;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class QuestionStatsRepository {

    private final EntityManager em;

    public List<DailyQuestionListItemViewModel> findQuestions() {
        return em.createQuery("""
                select new com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionListItemViewModel(
                    q.id,
                    interest.code,
                    q.questionText,
                    q.questionLevel,
                    q.currentRevisionNo,
                    q.deletedAt
                )
                from DailyQuestion q
                left join q.interest interest
                order by q.id
                """, DailyQuestionListItemViewModel.class)
                .getResultList();
    }

    public List<DailyQuestionRevisionStatsViewModel> findRevisionStats(long questionId) {
        return em.createQuery("""
                select new com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionRevisionStatsViewModel(
                    revision.id,
                    revision.revisionNo,
                    interest.code,
                    revision.questionText,
                    revision.questionLevel,
                    revision.empathyGuide,
                    revision.hintGuide,
                    revision.leadingQuestionGuide,
                    revision.deletedAt,
                    revision.effectiveFrom,
                    revision.sourceMigration,
                    count(exposure.id),
                    coalesce(sum(case when exposure.answeredAt is not null then 1 else 0 end), 0),
                    coalesce(sum(case when exposure.rerolledAt is not null then 1 else 0 end), 0),
                    coalesce(sum(case
                        when exposure.id is not null
                         and exposure.answeredAt is null
                         and exposure.rerolledAt is null
                        then 1 else 0 end), 0)
                )
                from DailyQuestionRevision revision
                left join revision.interest interest
                left join DailyQuestionExposure exposure
                    on exposure.dailyQuestionRevision = revision
                where revision.dailyQuestion.id = :questionId
                group by
                    revision.id,
                    revision.revisionNo,
                    interest.code,
                    revision.questionText,
                    revision.questionLevel,
                    revision.empathyGuide,
                    revision.hintGuide,
                    revision.leadingQuestionGuide,
                    revision.deletedAt,
                    revision.effectiveFrom,
                    revision.sourceMigration
                order by revision.revisionNo desc
                """, DailyQuestionRevisionStatsViewModel.class)
                .setParameter("questionId", questionId)
                .getResultList();
    }
}
