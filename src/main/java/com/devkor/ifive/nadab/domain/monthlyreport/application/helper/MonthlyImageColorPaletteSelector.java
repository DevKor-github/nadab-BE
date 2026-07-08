package com.devkor.ifive.nadab.domain.monthlyreport.application.helper;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyImageColorPalette;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class MonthlyImageColorPaletteSelector {

    private static final String SELECTION_SALT = "monthly-image-color-palette-v1";

    private MonthlyImageColorPaletteSelector() {
    }

    public static MonthlyImageColorPalette select(
            Long userId,
            Long reportId,
            LocalDate monthStartDate,
            List<MonthlyImageColorPalette> recentPalettes
    ) {
        Set<MonthlyImageColorPalette> excluded = recentPalettes == null
                ? Set.of()
                : recentPalettes.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toUnmodifiableSet());

        List<MonthlyImageColorPalette> candidates = Arrays.stream(MonthlyImageColorPalette.values())
                .filter(palette -> !excluded.contains(palette))
                .toList();
        if (candidates.isEmpty()) {
            candidates = List.of(MonthlyImageColorPalette.values());
        }

        int seed = Objects.hash(SELECTION_SALT, userId, reportId, monthStartDate);
        return candidates.get(Math.floorMod(seed, candidates.size()));
    }
}
