package com.devkor.ifive.nadab.domain.monthlyreport.application.helper;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyImageColorPalette;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlyImageColorPaletteSelectorTest {

    @Test
    void selects_deterministically_for_same_report() {
        MonthlyImageColorPalette first = MonthlyImageColorPaletteSelector.select(
                1L, 10L, LocalDate.of(2026, 5, 1), List.of()
        );
        MonthlyImageColorPalette second = MonthlyImageColorPaletteSelector.select(
                1L, 10L, LocalDate.of(2026, 5, 1), List.of()
        );

        assertThat(second).isEqualTo(first);
    }

    @Test
    void excludes_recent_palettes() {
        List<MonthlyImageColorPalette> recent = List.of(
                MonthlyImageColorPalette.FOREST_MIST,
                MonthlyImageColorPalette.OCEAN_LIGHT,
                MonthlyImageColorPalette.SUNSET_CLAY
        );

        MonthlyImageColorPalette selected = MonthlyImageColorPaletteSelector.select(
                1L, 10L, LocalDate.of(2026, 5, 1), recent
        );

        assertThat(recent).doesNotContain(selected);
    }

    @Test
    void reuses_all_palettes_when_every_candidate_is_excluded() {
        MonthlyImageColorPalette selected = MonthlyImageColorPaletteSelector.select(
                1L,
                10L,
                LocalDate.of(2026, 5, 1),
                List.of(MonthlyImageColorPalette.values())
        );

        assertThat(Arrays.asList(MonthlyImageColorPalette.values())).contains(selected);
    }
}
