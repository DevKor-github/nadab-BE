package com.devkor.ifive.nadab.infra.db;

import org.springframework.test.context.DynamicPropertyRegistry;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 테스트용 PostgreSQL 컨테이너를 관리하는 유틸리티 클래스.
 *
 * - Spring @DynamicPropertySource에서 호출해서 DataSource/Flyway 설정을 주입
 */
public final class PostgresTestContainer {

    private static final DockerImageName IMAGE = DockerImageName.parse("postgres:16-alpine");

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(IMAGE)
                    .withReuse(true); // 선택: 로컬에서 재사용(설정 필요)

    private PostgresTestContainer() {
        // utility class
    }

    public static void start() {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
    }

    public static void stop() {
        if (POSTGRES.isRunning()) {
            POSTGRES.stop();
        }
    }

    public static void registerProperties(DynamicPropertyRegistry registry) {
        start();

        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        registry.add("spring.flyway.url", POSTGRES::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRES::getUsername);
        registry.add("spring.flyway.password", POSTGRES::getPassword);

        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }
}
