package com.devkor.ifive.nadab.domain.monthlyreport.application.helper;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyImageStylePreset;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlyImagePresetSelectorTest {

    @Test
    void selects_deterministically_for_same_report() {
        MonthlyImageStylePreset first = MonthlyImagePresetSelector.select(
                1L, 10L, LocalDate.of(2026, 5, 1), List.of()
        );
        MonthlyImageStylePreset second = MonthlyImagePresetSelector.select(
                1L, 10L, LocalDate.of(2026, 5, 1), List.of()
        );

        assertThat(second).isEqualTo(first);
    }

    @Test
    void excludes_recent_presets() {
        List<MonthlyImageStylePreset> recent = List.of(
                MonthlyImageStylePreset.BOTANICAL_COLLAGE,
                MonthlyImageStylePreset.GLASS_AND_LIGHT,
                MonthlyImageStylePreset.INK_WASH
        );

        MonthlyImageStylePreset selected = MonthlyImagePresetSelector.select(
                1L, 10L, LocalDate.of(2026, 5, 1), recent
        );

        assertThat(recent).doesNotContain(selected);
    }

    @Test
    void reuses_all_presets_when_every_candidate_is_excluded() {
        MonthlyImageStylePreset selected = MonthlyImagePresetSelector.select(
                1L,
                10L,
                LocalDate.of(2026, 5, 1),
                List.of(MonthlyImageStylePreset.values())
        );

        assertThat(Arrays.asList(MonthlyImageStylePreset.values())).contains(selected);
    }
}
