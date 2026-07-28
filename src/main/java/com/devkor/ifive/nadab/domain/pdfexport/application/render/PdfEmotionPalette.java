package com.devkor.ifive.nadab.domain.pdfexport.application.render;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionCode;

import java.util.Map;

/**
 * 감정 태그·레이더 라벨용 색/한글명.
 */
final class PdfEmotionPalette {

    record Style(String colorHex, String label) {
    }

    private static final Style FALLBACK = new Style("#D4D4D4", "기타");

    private static final Map<EmotionCode, Style> STYLES = Map.of(
            EmotionCode.ACHIEVEMENT, new Style("#FFEF91", "성취"),
            EmotionCode.INTEREST, new Style("#FFD29B", "흥미"),
            EmotionCode.PEACE, new Style("#B4E7E2", "평온"),
            EmotionCode.PLEASURE, new Style("#FFA7A9", "즐거움"),
            EmotionCode.WILL, new Style("#ABC0F2", "의지"),
            EmotionCode.DEPRESSION, new Style("#D3A7FF", "우울"),
            EmotionCode.REGRET, new Style("#BDF2AB", "후회"),
            EmotionCode.ETC, FALLBACK
    );

    private PdfEmotionPalette() {
    }

    static Style of(EmotionCode code) {
        return code == null ? FALLBACK : STYLES.getOrDefault(code, FALLBACK);
    }

    /** 레이더 축 라벨용: 코드 문자열(String projection)로 조회. 매칭 실패 시 fallback. */
    static Style of(String code) {
        if (code == null) {
            return FALLBACK;
        }
        try {
            return of(EmotionCode.valueOf(code));
        } catch (IllegalArgumentException e) {
            return FALLBACK;
        }
    }
}