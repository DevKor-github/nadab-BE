package com.devkor.ifive.nadab.domain.monthlyreport.api.dto.response;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MyMonthlyReportLookupResponseV2Test {

    @Test
    void serializes_report_and_previous_report_with_report_id() throws Exception {
        MyMonthlyReportLookupResponseV2 response = new MyMonthlyReportLookupResponseV2(
                new MonthlyReportLocatorResponse(20L, 2, 5, MonthlyReportStatus.IN_PROGRESS),
                new MonthlyReportLocatorResponse(10L, 1, 4, MonthlyReportStatus.COMPLETED)
        );

        JsonNode json = new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(response));

        assertThat(json.path("report").path("reportId").asLong()).isEqualTo(20L);
        assertThat(json.path("report").path("version").asInt()).isEqualTo(2);
        assertThat(json.path("previousReport").path("reportId").asLong()).isEqualTo(10L);
        assertThat(json.path("previousReport").path("status").asText()).isEqualTo("COMPLETED");
        assertThat(json.path("report").has("id")).isFalse();
    }
}
