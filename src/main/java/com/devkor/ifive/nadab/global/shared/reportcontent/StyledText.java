package com.devkor.ifive.nadab.global.shared.reportcontent;

import java.util.ArrayList;
import java.util.List;

public record StyledText(
        List<Segment> segments
) {
    public String plainText() {
        if (segments == null) return "";
        StringBuilder sb = new StringBuilder();
        for (Segment s : segments) {
            if (s != null && s.text() != null) sb.append(s.text());
        }
        return sb.toString();
    }

    /**
     * DB/LLM 입력 방어용: null marks -> [] , null segment 제거 등
     */
    public StyledText normalized() {
        if (segments == null || segments.isEmpty()) {
            return new StyledText(List.of());
        }
        List<Segment> out = new ArrayList<>();
        for (Segment s : segments) {
            if (s == null) continue;
            String text = s.text() == null ? "" : s.text();
            List<Mark> marks = s.marks() == null ? List.of() : s.marks();
            out.add(new Segment(text, marks));
        }
        return new StyledText(List.copyOf(out));
    }
}
