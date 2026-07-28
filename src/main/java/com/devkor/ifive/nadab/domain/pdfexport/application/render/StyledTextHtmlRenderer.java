package com.devkor.ifive.nadab.domain.pdfexport.application.render;

import com.devkor.ifive.nadab.global.shared.reportcontent.Mark;
import com.devkor.ifive.nadab.global.shared.reportcontent.Segment;
import com.devkor.ifive.nadab.global.shared.reportcontent.StyledText;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * StyledText(Segment + Mark) → 인라인 XHTML.
 */
final class StyledTextHtmlRenderer {

    private StyledTextHtmlRenderer() {
    }

    /** 세그먼트들을 이어붙인 인라인 HTML. 텍스트는 모두 escape. */
    static String render(StyledText styledText) {
        if (styledText == null || styledText.segments() == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Segment segment : styledText.segments()) {
            if (segment == null || segment.text() == null || segment.text().isEmpty()) {
                continue;
            }
            String text = PdfHtml.escape(segment.text());
            String[] tags = markTags(segment.marks());
            sb.append(tags[0]).append(text).append(tags[1]);
        }
        return sb.toString();
    }

    /** 마크 종류(BOLD/HIGHLIGHT)는 구분하지 않고, 마크가 하나라도 있으면 볼드+형광펜(.hl). 없으면 일반. */
    private static String[] markTags(List<Mark> marks) {
        boolean marked = marks != null && !marks.isEmpty();
        return marked ? new String[]{"<span class=\"hl\">", "</span>"} : new String[]{"", ""};
    }

    /** 문장 1개: 측정용 plain + 렌더용 인라인 html(마크 span 유지). */
    record Sentence(String plain, String html) {
    }

    /** 종결부호(.?!) + (공백 run | 문자열 끝). 소수점·약어는 경계 아님. */
    private static final Pattern SENTENCE_END = Pattern.compile("[.?!](?:\\s+|$)");

    /**
     * StyledText 를 문장 단위로 분리(한 마디 = 문장별 박스).
     * 구분자(종결부호+뒤 공백)는 그 문장 끝에 붙고, 마크는 문장 경계로 잘려도 각 조각에 유지된다.
     */
    static List<Sentence> renderSentences(StyledText styledText) {
        List<Sentence> out = new ArrayList<>();
        if (styledText == null || styledText.segments() == null) {
            return out;
        }
        StringBuilder html = new StringBuilder();
        StringBuilder plain = new StringBuilder();
        for (Segment segment : styledText.segments()) {
            if (segment == null || segment.text() == null || segment.text().isEmpty()) {
                continue;
            }
            String[] tags = markTags(segment.marks());
            String open = tags[0];
            String close = tags[1];
            for (Part part : splitKeepingDelimiters(segment.text())) {
                html.append(open).append(PdfHtml.escape(part.text())).append(close);
                plain.append(part.text());
                if (part.boundary()) {
                    out.add(new Sentence(plain.toString(), html.toString()));
                    html = new StringBuilder();
                    plain = new StringBuilder();
                }
            }
        }
        if (plain.length() > 0) {          // 종결부호 없이 끝난 꼬리 문장
            out.add(new Sentence(plain.toString(), html.toString()));
        }
        return out;
    }

    private record Part(String text, boolean boundary) {
    }

    /** 사이 텍스트 + 구분자(경계표시) 순서 유지. */
    private static List<Part> splitKeepingDelimiters(String text) {
        List<Part> parts = new ArrayList<>();
        Matcher m = SENTENCE_END.matcher(text);
        int last = 0;
        while (m.find()) {
            if (m.start() > last) {
                parts.add(new Part(text.substring(last, m.start()), false));
            }
            parts.add(new Part(m.group(), true));   // 구분자 = 종결부호(+공백)
            last = m.end();
        }
        if (last < text.length()) {
            parts.add(new Part(text.substring(last), false));
        }
        return parts;
    }
}