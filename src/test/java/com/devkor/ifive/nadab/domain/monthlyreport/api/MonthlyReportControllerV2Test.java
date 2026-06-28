package com.devkor.ifive.nadab.domain.monthlyreport.api;

import com.devkor.ifive.nadab.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlyReportControllerV2Test {

    @Test
    void current_lookup_is_hidden_from_swagger() throws Exception {
        Method method = MonthlyReportControllerV2.class.getMethod(
                "getCurrentMonthlyReport",
                UserPrincipal.class
        );

        assertThat(method.isAnnotationPresent(Hidden.class)).isTrue();
        assertThat(method.getAnnotation(GetMapping.class).value()).containsExactly("/current");
    }

    @Test
    void locator_swagger_documents_null_status_and_version_routes() throws Exception {
        Method method = MonthlyReportControllerV2.class.getMethod(
                "getMyMonthlyReport",
                UserPrincipal.class
        );

        String description = method.getAnnotation(Operation.class).description();
        assertThat(description)
                .contains("두 슬롯은 서로 독립적으로 null일 수 있습니다")
                .contains("report.status: PENDING | IN_PROGRESS | TEXT_COMPLETED | COMPLETED | FAILED")
                .contains("previousReport.status: 존재하는 경우 항상 COMPLETED")
                .contains("/api/v1/monthly-report/{reportId}")
                .contains("/api/v2/monthly-report/{reportId}");
    }
}
