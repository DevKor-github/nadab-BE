package com.devkor.ifive.nadab.domain.monthlyreport.application.helper;

import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlySocialRankingItem;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlySocialSummaryContent;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlySocialInteractionCountDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlySocialSummaryCalculatorTest {

    @Test
    void hides_summary_when_total_interactions_are_less_than_ten() {
        MonthlySocialSummaryContent result = MonthlySocialSummaryCalculator.calculate(
                5,
                List.of(count(1L, "가", 5)),
                List.of(count(2L, "나", 4))
        );

        assertThat(result.visible()).isFalse();
        assertThat(result.month()).isEqualTo(5);
        assertThat(result.likeRanking()).isEmpty();
        assertThat(result.commentRanking()).isEmpty();
    }

    @Test
    void hides_summary_when_either_interaction_has_no_friend() {
        MonthlySocialSummaryContent result = MonthlySocialSummaryCalculator.calculate(
                5,
                List.of(count(1L, "가", 10)),
                List.of()
        );

        assertThat(result.visible()).isFalse();
        assertThat(result.likeRanking()).isEmpty();
        assertThat(result.commentRanking()).isEmpty();
    }

    @Test
    void exposes_summary_at_ten_interactions_and_keeps_available_friends() {
        MonthlySocialSummaryContent result = MonthlySocialSummaryCalculator.calculate(
                5,
                List.of(count(1L, "가", 6)),
                List.of(count(2L, "나", 3), count(3L, "다", 1))
        );

        assertThat(result.visible()).isTrue();
        assertThat(result.likeRanking()).extracting(MonthlySocialRankingItem::nickname)
                .containsExactly("가");
        assertThat(result.commentRanking()).extracting(MonthlySocialRankingItem::nickname)
                .containsExactly("나", "다");
        assertThat(result.commentRanking()).extracting(MonthlySocialRankingItem::displayOrder)
                .containsExactly(1, 2);
        assertThat(result.commentRanking()).extracting(MonthlySocialRankingItem::topRank)
                .containsExactly(true, false);
    }

    @Test
    void sorts_by_count_then_korean_name_and_highlights_all_joint_top_friends() {
        MonthlySocialSummaryContent result = MonthlySocialSummaryCalculator.calculate(
                5,
                List.of(
                        count(4L, "라", 4),
                        count(2L, "나", 4),
                        count(1L, "가", 4),
                        count(3L, "다", 3)
                ),
                List.of(
                        count(6L, "다", 5),
                        count(5L, "나", 5),
                        count(7L, "가", 4),
                        count(8L, "라", 4)
                )
        );

        assertThat(result.likeRanking()).extracting(MonthlySocialRankingItem::nickname)
                .containsExactly("가", "나", "라");
        assertThat(result.likeRanking()).extracting(MonthlySocialRankingItem::displayOrder)
                .containsExactly(1, 2, 3);
        assertThat(result.likeRanking()).allMatch(MonthlySocialRankingItem::topRank);

        assertThat(result.commentRanking()).extracting(MonthlySocialRankingItem::nickname)
                .containsExactly("나", "다", "가");
        assertThat(result.commentRanking()).extracting(MonthlySocialRankingItem::topRank)
                .containsExactly(true, true, false);
    }

    private MonthlySocialInteractionCountDto count(Long userId, String nickname, long count) {
        return new MonthlySocialInteractionCountDto(userId, nickname, null, null, count);
    }
}
