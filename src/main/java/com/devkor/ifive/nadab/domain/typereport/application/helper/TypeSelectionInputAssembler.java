package com.devkor.ifive.nadab.domain.typereport.application.helper;

import com.devkor.ifive.nadab.domain.typereport.core.dto.AnalysisTypeCandidateDto;
import com.devkor.ifive.nadab.domain.typereport.core.dto.PatternExtractionResultDto;

import java.util.List;

public final class TypeSelectionInputAssembler {

    private TypeSelectionInputAssembler() {}

    public static String assembleCandidates(List<AnalysisTypeCandidateDto> candidates) {
        if (candidates == null || candidates.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (AnalysisTypeCandidateDto c : candidates) {
            sb.append("- code: ").append(c.code()).append("\n")
                    .append("  name: ").append(c.name()).append("\n")
                    .append("  description: ").append(c.description()).append("\n")
                    .append("  hashtags: ").append(c.hashtag1()).append(" ").append(c.hashtag2()).append(" ").append(c.hashtag3()).append("\n\n");
        }
        return sb.toString().trim();
    }

    public static String assemblePatterns(PatternExtractionResultDto patternResult) {
        if (patternResult == null || patternResult.patterns() == null || patternResult.patterns().isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (PatternExtractionResultDto.PatternDto p : patternResult.patterns()) {
            sb.append("- label: ").append(p.label()).append("\n")
                    .append("  note: ").append(p.note()).append("\n")
                    .append("  evidence_ids: ").append(p.evidenceIds()).append("\n\n");
        }
        return sb.toString().trim();
    }
}

