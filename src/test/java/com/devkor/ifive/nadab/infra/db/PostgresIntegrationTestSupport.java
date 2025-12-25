package com.devkor.ifive.nadab.infra.db;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public class PostgresIntegrationTestSupport {

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        PostgresTestContainer.registerProperties(registry);
    }
}
