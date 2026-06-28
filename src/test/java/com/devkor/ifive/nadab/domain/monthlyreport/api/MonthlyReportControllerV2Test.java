package com.devkor.ifive.nadab.domain.monthlyreport.api;

import com.devkor.ifive.nadab.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Hidden;
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
}
