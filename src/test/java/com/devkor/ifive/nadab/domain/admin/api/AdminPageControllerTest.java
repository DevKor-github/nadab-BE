package com.devkor.ifive.nadab.domain.admin.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminPageControllerTest {

    @Test
    void returns_admin_logs_template_for_logs_tab() {
        AdminPageController controller = new AdminPageController();

        assertThat(controller.adminLogsPage()).isEqualTo("admin/logs");
    }
}
