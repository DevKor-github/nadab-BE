package com.devkor.ifive.nadab.domain.monthlyreport.application.helper;

import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyImagePromptContext;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyImageColorPalette;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyImageStylePreset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlyImagePromptComposerTest {

    private final MonthlyImagePromptComposer composer = new MonthlyImagePromptComposer();

    @ParameterizedTest
    @EnumSource(MonthlyImageStylePreset.class)
    void 모든_프리셋에_대해_공통_문맥과_스타일_지침을_조립한다(MonthlyImageStylePreset preset) {
        MonthlyImagePromptContext context = context(preset, "꾸준한 성장", "잠시 쉬어가도 괜찮아요", "성장");

        String prompt = composer.compose(context);

        assertThat(prompt)
                .contains("Monthly summary: 꾸준한 성장")
                .contains("Gentle comment summary: 잠시 쉬어가도 괜찮아요")
                .contains("Dominant keyword: 성장")
                .contains("Month: 2026-05-01 to 2026-05-31")
                .contains("Visual direction:")
                .contains("No people, no faces, no hands, no body parts.")
                .doesNotContain(preset.name());
    }

    @Test
    void 프리셋마다_서로_다른_시각_지침을_사용한다() {
        String botanical = composer.compose(context(
                MonthlyImageStylePreset.BOTANICAL_COLLAGE, "요약", "코멘트", "키워드"));
        String glass = composer.compose(context(
                MonthlyImageStylePreset.GLASS_AND_LIGHT, "요약", "코멘트", "키워드"));

        assertThat(botanical).contains("Botanical editorial collage");
        assertThat(glass).contains("Abstract translucent glass forms");
        assertThat(botanical).isNotEqualTo(glass);
    }

    @Test
    void 빈_리포트_문맥은_안전한_대체값으로_정규화한다() {
        MonthlyImagePromptContext context = context(
                MonthlyImageStylePreset.INK_WASH, null, "  ", "\t");

        String prompt = composer.compose(context);

        assertThat(prompt).contains("Monthly summary: Not provided")
                .contains("Gentle comment summary: Not provided")
                .contains("Dominant keyword: Not provided");
    }

    @ParameterizedTest
    @EnumSource(MonthlyImageColorPalette.class)
    void composes_a_distinct_direction_for_every_color_palette(MonthlyImageColorPalette palette) {
        MonthlyImagePromptContext context = new MonthlyImagePromptContext(
                "summary",
                "comment",
                "keyword",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                MonthlyImageStylePreset.MINIMAL_GEOMETRY,
                palette
        );

        String prompt = composer.compose(context);

        assertThat(prompt)
                .contains("Color direction:")
                .contains("The specified color direction is mandatory.")
                .contains("Do not default to lavender, pink, beige, peach")
                .doesNotContain(palette.name())
                .doesNotContain("feel calm, warm");
    }

    private MonthlyImagePromptContext context(
            MonthlyImageStylePreset preset,
            String summary,
            String commentSummary,
            String dominantKeyword
    ) {
        return new MonthlyImagePromptContext(
                summary,
                commentSummary,
                dominantKeyword,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                preset,
                MonthlyImageColorPalette.OCEAN_LIGHT
        );
    }
}
