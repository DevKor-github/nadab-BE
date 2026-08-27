package com.devkor.ifive.nadab.domain.stats.application.helper;

import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewQuery;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewRowViewModel;
import com.devkor.ifive.nadab.domain.stats.core.dto.question.DailyQuestionOverviewViewModel;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DailyQuestionOverviewCsvExporterTest {

    private final DailyQuestionOverviewCsvExporter exporter = new DailyQuestionOverviewCsvExporter();

    @Test
    void export_writes_excel_compatible_csv_and_neutralizes_formula_text() {
        DailyQuestionOverviewRowViewModel activeQuestion = new DailyQuestionOverviewRowViewModel(
                42L,
                InterestCode.PREFERENCE,
                "=SUM(A1:A2), \"질문\"",
                2,
                2,
                null,
                OffsetDateTime.parse("2026-08-26T12:00:00+09:00"),
                10L,
                4L,
                3L,
                3L,
                15L,
                6L,
                4L,
                5L
        );
        DailyQuestionOverviewRowViewModel inactiveQuestionWithoutExposure =
                new DailyQuestionOverviewRowViewModel(
                        43L,
                        InterestCode.EMOTION,
                        "일반 질문",
                        1,
                        1,
                        OffsetDateTime.parse("2026-08-27T09:00:00+09:00"),
                        null,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L
                );
        DailyQuestionOverviewViewModel viewModel = new DailyQuestionOverviewViewModel(
                List.of(activeQuestion, inactiveQuestionWithoutExposure),
                2,
                DailyQuestionOverviewQuery.defaults(),
                OffsetDateTime.parse("2026-08-25T12:00:00+09:00"),
                "2026-08-27 10:00:00"
        );

        byte[] csvBytes = exporter.export(viewModel);

        assertThat(csvBytes).startsWith((byte) 0xEF, (byte) 0xBB, (byte) 0xBF);
        assertThat(new String(csvBytes, StandardCharsets.UTF_8)).isEqualTo(
                "\uFEFF질문 ID,질문 문구,관심사 코드,관심사,레벨,상태,현재 Revision,현재 Revision 적용 시각,"
                        + "현재 Revision 노출,현재 Revision 답변,현재 Revision 답변율(%),현재 Revision 교체,"
                        + "현재 Revision 교체율(%),현재 Revision 미응답,전체 Revision 노출,전체 Revision 답변,"
                        + "전체 Revision 답변율(%),전체 Revision 교체,전체 Revision 미응답\r\n"
                        + "42,\"'=SUM(A1:A2), \"\"질문\"\"\",\"PREFERENCE\",\"취향\",2,\"ACTIVE\",2,"
                        + "\"2026-08-26T12:00:00+09:00\",10,4,40.0,3,30.0,3,15,6,40.0,4,5\r\n"
                        + "43,\"일반 질문\",\"EMOTION\",\"감정\",1,\"INACTIVE\",1,\"\","
                        + "0,0,,0,,0,0,0,,0,0\r\n"
        );
    }
}
