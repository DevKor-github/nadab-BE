package com.devkor.ifive.nadab.domain.admin.api.dto.response;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdminLogPageResponseTest {

    @Test
    void maps_spring_page_to_admin_log_page_response() {
        AdminLogPageResponse<String> response = AdminLogPageResponse.from(
                new PageImpl<>(
                        List.of("log-2"),
                        PageRequest.of(1, 1),
                        3
                )
        );

        assertThat(response.items()).containsExactly("log-2");
        assertThat(response.totalCount()).isEqualTo(3);
        assertThat(response.currentPage()).isEqualTo(2);
        assertThat(response.pageSize()).isEqualTo(1);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.hasPrevious()).isTrue();
        assertThat(response.hasNext()).isTrue();
    }

    @Test
    void reports_no_navigation_for_empty_page() {
        AdminLogPageResponse<String> response = AdminLogPageResponse.from(
                new PageImpl<>(List.of(), PageRequest.of(0, 20), 0)
        );

        assertThat(response.items()).isEmpty();
        assertThat(response.totalCount()).isZero();
        assertThat(response.totalPages()).isZero();
        assertThat(response.hasPrevious()).isFalse();
        assertThat(response.hasNext()).isFalse();
    }
}
