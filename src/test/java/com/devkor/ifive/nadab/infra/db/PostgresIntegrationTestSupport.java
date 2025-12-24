package com.devkor.ifive.nadab.infra.db;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@ExtendWith(PostgresContainerExtension.class)
public class PostgresIntegrationTestSupport {

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        PostgresTestContainer.registerProperties(registry);
    }
}
