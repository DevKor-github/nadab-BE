package com.devkor.ifive.nadab.domain.monthlyreport.application.helper;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyImageStylePreset;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class MonthlyImagePresetSelector {

    private MonthlyImagePresetSelector() {
    }

    public static MonthlyImageStylePreset select(
            Long userId,
            Long reportId,
            LocalDate monthStartDate,
            List<MonthlyImageStylePreset> recentPresets
    ) {
        Set<MonthlyImageStylePreset> excluded = recentPresets == null
                ? Set.of()
                : recentPresets.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toUnmodifiableSet());

        List<MonthlyImageStylePreset> candidates = Arrays.stream(MonthlyImageStylePreset.values())
                .filter(preset -> !excluded.contains(preset))
                .toList();
        if (candidates.isEmpty()) {
            candidates = List.of(MonthlyImageStylePreset.values());
        }

        int seed = Objects.hash(userId, reportId, monthStartDate);
        return candidates.get(Math.floorMod(seed, candidates.size()));
    }
}
