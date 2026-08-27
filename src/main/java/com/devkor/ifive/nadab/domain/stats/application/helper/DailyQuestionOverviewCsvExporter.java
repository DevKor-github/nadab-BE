package com.devkor.ifive.nadab.domain.stats.application.helper;

import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewRowViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewViewModel;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
public class DailyQuestionOverviewCsvExporter {

    private static final String UTF_8_BOM = "\uFEFF";
    private static final String LINE_SEPARATOR = "\r\n";
    private static final String HEADER = String.join(",", List.of(
            "질문 ID",
            "질문 문구",
            "관심사 코드",
            "관심사",
            "레벨",
            "상태",
            "현재 Revision",
            "현재 Revision 적용 시각",
            "현재 Revision 노출",
            "현재 Revision 답변",
            "현재 Revision 답변율(%)",
            "현재 Revision 교체",
            "현재 Revision 교체율(%)",
            "현재 Revision 미응답",
            "전체 Revision 노출",
            "전체 Revision 답변",
            "전체 Revision 답변율(%)",
            "전체 Revision 교체",
            "전체 Revision 미응답"
    ));

    public byte[] export(DailyQuestionOverviewViewModel viewModel) {
        StringBuilder csv = new StringBuilder(UTF_8_BOM.length() + HEADER.length() + viewModel.rows().size() * 256);
        csv.append(UTF_8_BOM).append(HEADER).append(LINE_SEPARATOR);
        viewModel.rows().forEach(row -> appendRow(csv, row));
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void appendRow(StringBuilder csv, DailyQuestionOverviewRowViewModel row) {
        List<String> cells = List.of(
                Long.toString(row.questionId()),
                textCell(row.questionText()),
                textCell(row.interestCode() == null ? "" : row.interestCode().name()),
                textCell(row.interestCode() == null ? "" : row.interestCode().displayNameKo()),
                Integer.toString(row.questionLevel()),
                textCell(row.active() ? "ACTIVE" : "INACTIVE"),
                Integer.toString(row.currentRevisionNo()),
                textCell(row.currentRevisionEffectiveFrom() == null
                        ? ""
                        : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(row.currentRevisionEffectiveFrom())),
                Long.toString(row.currentExposureCount()),
                Long.toString(row.currentAnsweredCount()),
                rateCell(row.currentExposureCount(), row.currentAnswerRatePercent()),
                Long.toString(row.currentRerolledCount()),
                rateCell(row.currentExposureCount(), row.currentRerollRatePercent()),
                Long.toString(row.currentUnansweredCount()),
                Long.toString(row.totalExposureCount()),
                Long.toString(row.totalAnsweredCount()),
                rateCell(row.totalExposureCount(), row.totalAnswerRatePercent()),
                Long.toString(row.totalRerolledCount()),
                Long.toString(row.totalUnansweredCount())
        );
        csv.append(String.join(",", cells)).append(LINE_SEPARATOR);
    }

    private String rateCell(long exposureCount, double ratePercent) {
        return exposureCount == 0L ? "" : String.format(Locale.ROOT, "%.1f", ratePercent);
    }

    private String textCell(String value) {
        String safeValue = neutralizeFormula(value == null ? "" : value);
        return '"' + safeValue.replace("\"", "\"\"") + '"';
    }

    private String neutralizeFormula(String value) {
        int firstContentIndex = 0;
        while (firstContentIndex < value.length() && Character.isWhitespace(value.charAt(firstContentIndex))) {
            firstContentIndex++;
        }
        if (firstContentIndex < value.length()
                && "=+-@".indexOf(value.charAt(firstContentIndex)) >= 0) {
            return "'" + value;
        }
        return value;
    }
}
