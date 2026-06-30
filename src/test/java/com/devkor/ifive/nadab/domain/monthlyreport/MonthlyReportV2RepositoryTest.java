package com.devkor.ifive.nadab.domain.monthlyreport;

import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlySocialRankingItem;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlySocialSummaryContent;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlyReportV2ContentFactory;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyImageColorPalette;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyImageStylePreset;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportComparisonType;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportImageStatus;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MonthlyReportV2RepositoryTest extends PostgresIntegrationTestSupport {

    @Autowired
    MonthlyReportV2Repository monthlyReportV2Repository;

    @Autowired
    TestEntityManager em;

    @Test
    void create_pending_initializes_empty_social_summary_for_report_month() {
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

        MonthlyReportV2 reloaded = monthlyReportV2Repository.findById(report.getId()).orElseThrow();

        assertThat(reloaded.getSocialSummary().visible()).isFalse();
        assertThat(reloaded.getSocialSummary().month()).isEqualTo(5);
        assertThat(reloaded.getSocialSummary().likeRanking()).isEmpty();
        assertThat(reloaded.getSocialSummary().commentRanking()).isEmpty();
    }

    @Test
    void update_social_summary_saves_display_order_and_joint_top_rank() {
        User user = new UserBuilder(em).build();
        MonthlyReportV2 report = monthlyReportV2Repository.save(MonthlyReportV2.createPending(
                user,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                LocalDate.of(2026, 6, 1),
                MonthlyReportComparisonType.BASELINE
        ));
        report.updateSocialSummary(new MonthlySocialSummaryContent(
                true,
                5,
                List.of(
                        new MonthlySocialRankingItem(1, 10L, "가", null, null, true),
                        new MonthlySocialRankingItem(2, 11L, "나", null, null, true)
                ),
                List.of(new MonthlySocialRankingItem(1, 12L, "다", null, null, true))
        ));
        em.flush();
        em.clear();

        MonthlyReportV2 reloaded = monthlyReportV2Repository.findById(report.getId()).orElseThrow();

        assertThat(reloaded.getSocialSummary().visible()).isTrue();
        assertThat(reloaded.getSocialSummary().likeRanking())
                .extracting(MonthlySocialRankingItem::displayOrder)
                .containsExactly(1, 2);
        assertThat(reloaded.getSocialSummary().likeRanking())
                .allMatch(MonthlySocialRankingItem::topRank);
    }

    @Test
    void saves_visual_preset_and_finds_three_most_recent_completed_values() {
        User user = new UserBuilder(em).build();
        completedReport(user, LocalDate.of(2026, 2, 1), MonthlyImageStylePreset.INK_WASH,
                MonthlyImageColorPalette.FOREST_MIST);
        completedReport(user, LocalDate.of(2026, 3, 1), MonthlyImageStylePreset.GLASS_AND_LIGHT,
                MonthlyImageColorPalette.OCEAN_LIGHT);
        completedReport(user, LocalDate.of(2026, 4, 1), MonthlyImageStylePreset.PAPER_CUT_LAYERS,
                MonthlyImageColorPalette.SUNSET_CLAY);
        MonthlyReportV2 may = completedReport(user, LocalDate.of(2026, 5, 1),
                MonthlyImageStylePreset.BOTANICAL_COLLAGE, MonthlyImageColorPalette.MOON_VIOLET);
        MonthlyReportV2 current = monthlyReportV2Repository.save(MonthlyReportV2.createPending(
                user,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                LocalDate.of(2026, 7, 1),
                MonthlyReportComparisonType.COMPARISON
        ));
        em.flush();
        em.clear();

        List<MonthlyImageStylePreset> recent = monthlyReportV2Repository.findRecentCompletedImagePromptVariants(
                user.getId(),
                current.getId(),
                PageRequest.of(0, 3)
        );
        List<MonthlyImageColorPalette> recentPalettes =
                monthlyReportV2Repository.findRecentCompletedImageColorPalettes(
                        user.getId(),
                        current.getId(),
                        PageRequest.of(0, 3)
                );
        MonthlyReportV2 reloadedMay = monthlyReportV2Repository.findById(may.getId()).orElseThrow();

        assertThat(recent).containsExactly(
                MonthlyImageStylePreset.BOTANICAL_COLLAGE,
                MonthlyImageStylePreset.PAPER_CUT_LAYERS,
                MonthlyImageStylePreset.GLASS_AND_LIGHT
        );
        assertThat(recentPalettes).containsExactly(
                MonthlyImageColorPalette.MOON_VIOLET,
                MonthlyImageColorPalette.SUNSET_CLAY,
                MonthlyImageColorPalette.OCEAN_LIGHT
        );
        assertThat(reloadedMay.getImagePromptVariant()).isEqualTo(MonthlyImageStylePreset.BOTANICAL_COLLAGE);
        assertThat(reloadedMay.getImageColorPalette()).isEqualTo(MonthlyImageColorPalette.MOON_VIOLET);
    }

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
        assertThat(reloaded.getSocialSummary().visible()).isTrue();
        assertThat(reloaded.getSocialSummary().likeRanking()).singleElement()
                .satisfies(item -> {
                    assertThat(item.displayOrder()).isEqualTo(1);
                    assertThat(item.nickname()).isEqualTo("가");
                    assertThat(item.topRank()).isTrue();
                });
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
                "{\"visible\":true,\"month\":5,\"likeRanking\":[{\"displayOrder\":1,\"userId\":10,\"nickname\":\"가\",\"profileImageKey\":null,\"defaultProfileType\":null,\"topRank\":true}],\"commentRanking\":[{\"displayOrder\":1,\"userId\":11,\"nickname\":\"나\",\"profileImageKey\":null,\"defaultProfileType\":null,\"topRank\":true}]}",
                MonthlyReportStatus.TEXT_COMPLETED.name()
        );
    }

    private MonthlyReportV2 completedReport(
            User user,
            LocalDate monthStartDate,
            MonthlyImageStylePreset preset,
            MonthlyImageColorPalette colorPalette
    ) {
        MonthlyReportV2 report = MonthlyReportV2.create(
                user,
                monthStartDate,
                monthStartDate.withDayOfMonth(monthStartDate.lengthOfMonth()),
                MonthlyReportV2ContentFactory.empty(),
                monthStartDate.plusMonths(1),
                MonthlyReportStatus.COMPLETED,
                MonthlyReportImageStatus.COMPLETED,
                MonthlyReportComparisonType.COMPARISON
        );
        report.assignImagePromptVariant(preset);
        report.assignImageColorPalette(colorPalette);
        return monthlyReportV2Repository.save(report);
    }
}
