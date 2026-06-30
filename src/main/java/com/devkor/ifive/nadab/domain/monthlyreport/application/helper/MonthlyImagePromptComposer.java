package com.devkor.ifive.nadab.domain.monthlyreport.application.helper;

import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyImagePromptContext;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyImageColorPalette;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyImageStylePreset;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class MonthlyImagePromptComposer {

    private static final Map<MonthlyImageStylePreset, String> STYLE_DIRECTIONS = styleDirections();
    private static final Map<MonthlyImageColorPalette, String> COLOR_DIRECTIONS = colorDirections();

    public String compose(MonthlyImagePromptContext context) {
        String styleDirection = STYLE_DIRECTIONS.get(context.stylePreset());
        if (styleDirection == null) {
            throw new IllegalArgumentException("Unsupported monthly image style preset: " + context.stylePreset());
        }
        String colorDirection = COLOR_DIRECTIONS.get(context.colorPalette());
        if (colorDirection == null) {
            throw new IllegalArgumentException("Unsupported monthly image color palette: " + context.colorPalette());
        }

        return """
                Create a premium abstract monthly self-reflection cover image for a Korean journaling app.

                Report context:
                - Monthly summary: %s
                - Gentle comment summary: %s
                - Dominant keyword: %s
                - Month: %s to %s

                Visual direction:
                %s

                Color direction:
                %s

                The specified color direction is mandatory. Use it to control the background, dominant forms, lighting, and accents. Preserve clear hue separation and recognizable palette identity. Do not default to lavender, pink, beige, peach, or a generic pastel gradient unless those colors are explicitly included in the selected color direction.

                Interpret the report context symbolically through the selected visual direction. Vary the objects, color relationships, lighting, spatial rhythm, and composition according to the emotional meaning. Do not fall back to a generic fixed scene.

                The image should feel calm, reflective, comforting, refined, and suitable for a premium mobile app monthly report card. Keep a clear focal point and enough negative space.

                Strict constraints:
                No people, no faces, no hands, no body parts.
                No readable text, no letters, no Korean characters, no English characters, no numbers, no symbols, no handwriting, no typography.
                No UI, no logos, no brand marks, no watermarks.
                No medical, therapy, counseling, hospital, or diagnosis-related imagery.
                No dark, horror, gloomy, childish, or overly dramatic mood.
                """.formatted(
                context.summary(),
                context.commentSummary(),
                context.dominantKeyword(),
                context.monthStartDate(),
                context.monthEndDate(),
                styleDirection,
                colorDirection
        );
    }

    private static Map<MonthlyImageStylePreset, String> styleDirections() {
        EnumMap<MonthlyImageStylePreset, String> directions =
                new EnumMap<>(MonthlyImageStylePreset.class);
        directions.put(MonthlyImageStylePreset.BOTANICAL_COLLAGE, """
                Botanical editorial collage using abstract leaves, petals, seeds, and organic silhouettes.
                Layer tactile paper textures in the selected color direction with an asymmetrical, airy composition.
                """);
        directions.put(MonthlyImageStylePreset.PAPER_CUT_LAYERS, """
                Dimensional paper-cut illustration built from softly layered shapes and carefully controlled shadows.
                Use depth, openings, and overlapping planes to express the emotional progression without depicting a literal scene.
                """);
        directions.put(MonthlyImageStylePreset.GLASS_AND_LIGHT, """
                Abstract translucent glass forms with gentle refraction, luminous gradients, and soft reflected light.
                Keep the materials elegant and weightless, with subtle depth and no sharp or hazardous appearance.
                """);
        directions.put(MonthlyImageStylePreset.MINIMAL_GEOMETRY, """
                Minimal geometric editorial composition using circles, arcs, planes, and balanced spatial relationships.
                Use the selected color direction with precise rhythm and soft dimensional lighting rather than flat iconography.
                """);
        directions.put(MonthlyImageStylePreset.INK_WASH, """
                Contemporary abstract ink-wash artwork with diluted pigment, quiet brush diffusion, and generous open space.
                Keep the mood light and comforting, varying pigment intensity within the selected colors without calligraphy or written marks.
                """);
        directions.put(MonthlyImageStylePreset.DREAMLIKE_LANDSCAPE, """
                Dreamlike abstract landscape made of gentle horizons, atmospheric color fields, mist, and ambient light.
                Suggest an inner emotional journey without recognizable locations, people, buildings, or dramatic weather.
                """);
        directions.put(MonthlyImageStylePreset.TEXTILE_PATTERN, """
                Artisanal textile-inspired abstraction using woven rhythm, soft fibers, stitched-like paths, and layered fabric forms.
                Keep it sophisticated and editorial, avoiding literal lettering, symbols, decorative text, or busy repetition.
                """);
        directions.put(MonthlyImageStylePreset.CERAMIC_OBJECTS, """
                Sculptural ceramic still life using abstract vessels, rounded forms, matte glaze, and controlled studio light.
                Arrange the objects as an emotional metaphor with an uncluttered composition and no human or household narrative.
                """);
        return Map.copyOf(directions);
    }

    private static Map<MonthlyImageColorPalette, String> colorDirections() {
        EnumMap<MonthlyImageColorPalette, String> directions =
                new EnumMap<>(MonthlyImageColorPalette.class);
        directions.put(MonthlyImageColorPalette.FOREST_MIST, """
                Forest mist palette: sage green and deep teal as dominant hues, moss green accents, and foggy cream highlights.
                Keep the temperature cool and botanical, avoiding pink or purple casts.
                """);
        directions.put(MonthlyImageColorPalette.OCEAN_LIGHT, """
                Ocean light palette: cobalt blue and clear cyan as dominant hues, turquoise transitions, and crisp silver-white highlights.
                Keep the result fresh and luminous, avoiding beige or dusty pink casts.
                """);
        directions.put(MonthlyImageColorPalette.SUNSET_CLAY, """
                Sunset clay palette: terracotta and burnt orange as dominant hues, apricot midtones, and restrained burgundy accents.
                Keep the colors earthy and sunlit rather than pink, lavender, or candy-like.
                """);
        directions.put(MonthlyImageColorPalette.MOON_VIOLET, """
                Moon violet palette: indigo and violet as dominant hues, periwinkle transitions, and cool pearl highlights.
                Keep sufficient luminosity for a comforting mood while preserving a distinctly nocturnal violet identity.
                """);
        directions.put(MonthlyImageColorPalette.CITRUS_BREEZE, """
                Citrus breeze palette: lemon yellow and fresh mint as dominant hues, lime-tinted transitions, and coral accents.
                Keep the colors clear and energetic with a light neutral background, avoiding lavender or muted beige dominance.
                """);
        directions.put(MonthlyImageColorPalette.EARTH_NEUTRAL, """
                Earth neutral palette: ivory and stone gray as the foundation, taupe and soft charcoal as dominant forms, and subtle bronze accents.
                Keep saturation intentionally low and avoid pink, purple, or blue color casts.
                """);
        directions.put(MonthlyImageColorPalette.JEWEL_GLOW, """
                Jewel glow palette: emerald and sapphire as dominant hues, amethyst accents, and bright crystal-like highlights.
                Use rich but balanced saturation against a clean light ground, avoiding a generic pastel treatment.
                """);
        directions.put(MonthlyImageColorPalette.ROSE_DAWN, """
                Rose dawn palette: rose and peach as dominant hues, soft coral transitions, and pale sky-blue highlights.
                Keep the combination airy and sunrise-like while maintaining clear contrast between warm and cool colors.
                """);
        return Map.copyOf(directions);
    }
}
