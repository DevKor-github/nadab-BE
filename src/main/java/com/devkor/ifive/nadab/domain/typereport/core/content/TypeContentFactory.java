package com.devkor.ifive.nadab.domain.typereport.core.content;

import com.devkor.ifive.nadab.global.shared.reportcontent.Segment;
import com.devkor.ifive.nadab.global.shared.reportcontent.StyledText;

import java.util.List;

public final class TypeContentFactory {

    private TypeContentFactory() {
    }

    public static TypeTextContent emptyText() {
        return new TypeTextContent(new StyledText(List.of(new Segment("", List.of()))));
    }

    public static TypeTextContent fromPlainText(String text) {
        return new TypeTextContent(new StyledText(
                List.of(new Segment(text == null ? "" : text, List.of()))
        ));
    }

    public static TypeEmotionStatsContent emptyEmotionStats() {
        return new TypeEmotionStatsContent(0, null, 0, List.of());
    }
}
