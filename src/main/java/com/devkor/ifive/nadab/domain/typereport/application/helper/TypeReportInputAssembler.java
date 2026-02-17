package com.devkor.ifive.nadab.domain.typereport.application.helper;

import com.devkor.ifive.nadab.domain.typereport.core.dto.AnalysisTypeCandidateDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.EvidenceCardDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.PatternExtractionResultDto;

import java.util.List;

public final class TypeReportInputAssembler {

    private TypeReportInputAssembler() {}

    public static String assembleSelectedType(AnalysisTypeCandidateDto type) {
        if (type == null) return "";
        return """
                code: %s
                name: %s
                description: %s
                hashtags: %s %s %s
                """.formatted(
                n(type.code()), n(type.name()), n(type.description()),
                n(type.hashtag1()), n(type.hashtag2()), n(type.hashtag3())
        ).trim();
    }

    public static String assemblePatterns(PatternExtractionResultDto patterns) {
        if (patterns == null || patterns.patterns() == null || patterns.patterns().isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (PatternExtractionResultDto.PatternDto p : patterns.patterns()) {
            sb.append("- label: ").append(n(p.label())).append("\n")
                    .append("  note: ").append(n(p.note())).append("\n")
                    .append("  evidence_ids: ").append(p.evidenceIds()).append("\n\n");
        }
        return sb.toString().trim();
    }

    public static String assembleEvidenceCards(List<EvidenceCardDto> cards) {
        if (cards == null || cards.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (EvidenceCardDto c : cards) {
            sb.append("- id: ").append(n(c.id())).append("\n")
                    .append("  date: ").append(c.date() == null ? "N/A" : c.date()).append("\n")
                    .append("  card: ").append(n(c.card())).append("\n\n");
        }
        return sb.toString().trim();
    }

    private static String n(String s) {
        if (s == null) return "";
        return s.trim();
    }
}