package com.devkor.ifive.nadab.domain.typereport.core.content;

import com.devkor.ifive.nadab.global.shared.reportcontent.Segment;

import java.util.regex.Pattern;

public final class TypeReportContentFormat {

    private static final Pattern UNSUPPORTED_MARKS_TAG =
            Pattern.compile("<\\s*/?\\s*marks?\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern LEGACY_MARKS_TAG = Pattern.compile(
            "<\\s*marks?\\s+text=\"([^\"]*)\"\\s+marks=\\[[^\\]]*]\\s*/?>",
            Pattern.CASE_INSENSITIVE
    );

    private TypeReportContentFormat() {}

    public static boolean containsUnsupportedMarkup(String text) {
        return text != null && UNSUPPORTED_MARKS_TAG.matcher(text).find();
    }

    public static String sanitizeLegacyMarkup(String text) {
        return text == null ? null : LEGACY_MARKS_TAG.matcher(text).replaceAll("$1");
    }

    public static boolean containsUnsupportedMarkup(TypeTextContent content) {
        if (content == null || content.styledText() == null || content.styledText().segments() == null) {
            return false;
        }

        return content.styledText().segments().stream()
                .filter(segment -> segment != null)
                .map(Segment::text)
                .anyMatch(TypeReportContentFormat::containsUnsupportedMarkup);
    }
}
