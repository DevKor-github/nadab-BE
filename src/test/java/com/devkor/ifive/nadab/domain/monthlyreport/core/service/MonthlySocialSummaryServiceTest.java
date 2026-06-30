package com.devkor.ifive.nadab.domain.monthlyreport.core.service;

import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlySocialSummaryContent;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlySocialInteractionCountDto;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlySocialQueryRepository;
import com.devkor.ifive.nadab.global.shared.util.dto.MonthRangeDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlySocialSummaryServiceTest {

    @Mock
    MonthlySocialQueryRepository monthlySocialQueryRepository;

    @Test
    void builds_summary_with_seoul_month_boundaries() {
        MonthlySocialSummaryService service = new MonthlySocialSummaryService(monthlySocialQueryRepository);
        MonthRangeDto range = new MonthRangeDto(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31)
        );
        OffsetDateTime startAt = OffsetDateTime.of(2026, 5, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9));
        OffsetDateTime endAt = OffsetDateTime.of(2026, 6, 1, 0, 0, 0, 0, ZoneOffset.ofHours(9));
        List<MonthlySocialInteractionCountDto> likes = List.of(count(1L, "가", 6));
        List<MonthlySocialInteractionCountDto> comments = List.of(count(2L, "나", 4));
        when(monthlySocialQueryRepository.countReceivedLikesByFriend(10L, startAt, endAt))
                .thenReturn(likes);
        when(monthlySocialQueryRepository.countReceivedCommentsByFriend(10L, startAt, endAt))
                .thenReturn(comments);

        MonthlySocialSummaryContent result = service.buildSocialSummary(10L, range);

        assertThat(result.visible()).isTrue();
        assertThat(result.month()).isEqualTo(5);
        assertThat(result.likeRanking()).singleElement()
                .satisfies(item -> assertThat(item.nickname()).isEqualTo("가"));
        assertThat(result.commentRanking()).singleElement()
                .satisfies(item -> assertThat(item.nickname()).isEqualTo("나"));
        verify(monthlySocialQueryRepository).countReceivedLikesByFriend(10L, startAt, endAt);
        verify(monthlySocialQueryRepository).countReceivedCommentsByFriend(10L, startAt, endAt);
    }

    private MonthlySocialInteractionCountDto count(Long userId, String nickname, long count) {
        return new MonthlySocialInteractionCountDto(userId, nickname, null, null, count);
    }
}
