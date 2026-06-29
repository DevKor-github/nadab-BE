package com.devkor.ifive.nadab.domain.monthlyreport.application.helper;

import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlyImagePromptContext;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyImageStylePreset;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class MonthlyImagePromptComposer {

    private static final Map<MonthlyImageStylePreset, String> STYLE_DIRECTIONS = styleDirections();

    public String compose(MonthlyImagePromptContext context) {
        String styleDirection = STYLE_DIRECTIONS.get(context.stylePreset());
        if (styleDirection == null) {
            throw new IllegalArgumentException("Unsupported monthly image style preset: " + context.stylePreset());
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

                Interpret the report context symbolically through the selected visual direction. Vary the objects, color relationships, lighting, spatial rhythm, and composition according to the emotional meaning. Do not fall back to a generic fixed scene.

                The image should feel calm, warm, reflective, comforting, refined, and suitable for a premium mobile app monthly report card. Keep a clear focal point and enough negative space.

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
                styleDirection
        );
    }

    private static Map<MonthlyImageStylePreset, String> styleDirections() {
        EnumMap<MonthlyImageStylePreset, String> directions =
                new EnumMap<>(MonthlyImageStylePreset.class);
        directions.put(MonthlyImageStylePreset.BOTANICAL_COLLAGE, """
                Botanical editorial collage using abstract leaves, petals, seeds, and organic silhouettes.
                Layer tactile paper textures with a restrained natural palette and an asymmetrical, airy composition.
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
                Use a refined limited palette, precise rhythm, and soft dimensional lighting rather than flat iconography.
                """);
        directions.put(MonthlyImageStylePreset.INK_WASH, """
                Contemporary abstract ink-wash artwork with diluted pigment, quiet brush diffusion, and generous open space.
                Keep the mood light and comforting, using restrained tonal variation without calligraphy or written marks.
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
                Sculptural ceramic still life using abstract vessels, rounded forms, matte glaze, and warm studio light.
                Arrange the objects as an emotional metaphor with an uncluttered composition and no human or household narrative.
                """);
        return Map.copyOf(directions);
    }
}
