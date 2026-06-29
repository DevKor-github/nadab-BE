package com.devkor.ifive.nadab.domain.monthlyreport.core.dto;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyImageColorPalette;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyImageStylePreset;

import java.util.Objects;

public record MonthlyImageVisualPreset(
        MonthlyImageStylePreset stylePreset,
        MonthlyImageColorPalette colorPalette
) {
    public MonthlyImageVisualPreset {
        Objects.requireNonNull(stylePreset, "stylePreset must not be null");
        Objects.requireNonNull(colorPalette, "colorPalette must not be null");
    }
}
