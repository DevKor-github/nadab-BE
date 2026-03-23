package com.devkor.ifive.nadab.domain.typereport.core.content;

import com.devkor.ifive.nadab.global.shared.reportcontent.StyledText;

public record TypeTextContent(
        StyledText styledText
) {
    public TypeTextContent normalized() {
        StyledText normalized = styledText == null
                ? new StyledText(java.util.List.of())
                : styledText.normalized();
        return new TypeTextContent(normalized);
    }

    public String plainText() {
        return styledText == null ? "" : styledText.plainText();
    }
}
