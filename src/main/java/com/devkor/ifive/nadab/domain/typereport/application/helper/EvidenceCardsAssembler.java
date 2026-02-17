package com.devkor.ifive.nadab.domain.typereport.application.helper;

import com.devkor.ifive.nadab.domain.typereport.core.dto.EvidenceCardDto;

import java.util.List;

public final class EvidenceCardsAssembler {

    private EvidenceCardsAssembler() {}

    public static String assembleForPrompt(List<EvidenceCardDto> cards) {
        if (cards == null || cards.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (EvidenceCardDto c : cards) {
            if (c == null) continue;
            String id = c.id();
            String date = c.date() == null ? "N/A" : c.date().toString();
            String card = normalize(c.card());
            sb.append("- id: ").append(id).append("\n")
                    .append("  date: ").append(date).append("\n")
                    .append("  card: ").append(card).append("\n\n");
        }
        return sb.toString().trim();
    }

    private static String normalize(String s) {
        if (s == null) return "";
        String t = s.trim();
        return t.isBlank() ? "" : t;
    }
}
