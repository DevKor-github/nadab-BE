package com.devkor.ifive.nadab.domain.pdfexport.application.render;

/**
 * XHTML 조립 유틸. openhtmltopdf는 well-formed XML만 파싱하므로 사용자 콘텐츠는 반드시 escape 한다.
 */
final class PdfHtml {

    private PdfHtml() {
    }

    /** 텍스트 노드/속성값용 escape. null → 빈 문자열. */
    static String escape(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(raw.length() + 16);
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}