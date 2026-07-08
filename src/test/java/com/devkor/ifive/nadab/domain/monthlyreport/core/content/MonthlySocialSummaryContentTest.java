package com.devkor.ifive.nadab.domain.monthlyreport.core.content;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlySocialSummaryContentTest {

    @Test
    void normalized_limits_rankings_and_sanitizes_month() {
        MonthlySocialSummaryContent content = new MonthlySocialSummaryContent(
                true,
                13,
                List.of(item(1), item(2), item(3), item(4)),
                null
        );

        MonthlySocialSummaryContent normalized = content.normalized();

        assertThat(normalized.month()).isEqualTo(12);
        assertThat(normalized.likeRanking()).extracting(MonthlySocialRankingItem::displayOrder)
                .containsExactly(1, 2, 3);
        assertThat(normalized.commentRanking()).isEmpty();
    }

    private MonthlySocialRankingItem item(int rank) {
        return new MonthlySocialRankingItem(rank, (long) rank, "friend" + rank, null, null, rank == 1);
    }
}
