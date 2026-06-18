package com.devkor.ifive.nadab.domain.monthlyreport;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportComparisonType;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlyReportV2Repository;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.infra.builder.UserBuilder;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MonthlyReportV2RepositoryTest extends PostgresIntegrationTestSupport {

    @Autowired
    MonthlyReportV2Repository monthlyReportV2Repository;

    @Autowired
    TestEntityManager em;

    @Test
    void update_content_saves_emotion_comparison_snapshot() {
        User user = new UserBuilder(em).build();
        MonthlyReportV2 report = monthlyReportV2Repository.save(MonthlyReportV2.createPending(
                user,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                LocalDate.of(2026, 6, 1),
                MonthlyReportComparisonType.COMPARISON
        ));
        em.flush();
        em.clear();

        int updated = updateContent(
                report.getId(),
                "{\"previousReportId\":10,\"previousMonth\":4,\"previousEmotionStats\":{\"totalCount\":10,\"dominantEmotionCode\":\"ACHIEVEMENT\",\"positivePercent\":57,\"emotions\":[]},\"positivePercentPointChange\":14}"
        );
        em.flush();
        em.clear();

        MonthlyReportV2 reloaded = monthlyReportV2Repository.findById(report.getId()).orElseThrow();

        assertThat(updated).isEqualTo(1);
        assertThat(reloaded.getEmotionComparison()).isNotNull();
        assertThat(reloaded.getEmotionComparison().previousReportId()).isEqualTo(10L);
        assertThat(reloaded.getEmotionComparison().previousMonth()).isEqualTo(4);
        assertThat(reloaded.getEmotionComparison().previousEmotionStats().positivePercent()).isEqualTo(57);
        assertThat(reloaded.getEmotionComparison().positivePercentPointChange()).isEqualTo(14);
    }

    @Test
    void update_content_keeps_emotion_comparison_null_for_baseline() {
        User user = new UserBuilder(em).build();
        MonthlyReportV2 report = monthlyReportV2Repository.save(MonthlyReportV2.createPending(
                user,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                LocalDate.of(2026, 6, 1),
                MonthlyReportComparisonType.BASELINE
        ));
        em.flush();
        em.clear();

        int updated = updateContent(report.getId(), null);
        em.flush();
        em.clear();

        MonthlyReportV2 reloaded = monthlyReportV2Repository.findById(report.getId()).orElseThrow();

        assertThat(updated).isEqualTo(1);
        assertThat(reloaded.getEmotionComparison()).isNull();
    }

    private int updateContent(Long reportId, String emotionComparisonJson) {
        return monthlyReportV2Repository.updateContent(
                reportId,
                "{\"summary\":\"월간 요약\",\"commentSummary\":\"한마디 요약\",\"dominantKeyword\":\"도전\",\"emotionTrend\":\"도전을 중심으로 감정이 변했어요\",\"discovered\":{\"segments\":[]},\"comment\":{\"segments\":[]}}",
                "{\"styledText\":{\"segments\":[]}}",
                "월간 요약",
                "한마디 요약",
                "도전",
                "{\"totalCount\":10,\"dominantEmotionCode\":\"ACHIEVEMENT\",\"positivePercent\":71,\"emotions\":[]}",
                "{\"interests\":[]}",
                emotionComparisonJson,
                MonthlyReportStatus.TEXT_COMPLETED.name()
        );
    }
}
