package com.devkor.ifive.nadab.domain.auth.core.repository;

import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class WithdrawalReasonLegacyTableMigrationTest extends PostgresIntegrationTestSupport {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void marks_legacy_table_as_rollback_only_without_removing_it() {
        String tableComment = jdbcTemplate.queryForObject(
                "SELECT obj_description('user_withdrawal_reasons'::regclass)",
                String.class
        );

        assertThat(tableComment)
                .contains("LEGACY")
                .contains("rollback compatibility only")
                .contains("withdrawal_events")
                .contains("withdrawal_event_reasons");
    }
}
