package com.devkor.ifive.nadab.infra.db;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit5 Extension: 테스트 클래스 시작/종료 시점에 컨테이너를 기동/중단.
 *
 * - 컨테이너 기동 자체는 PostgresTestContainer가 담당
 * - 이 Extension은 라이프사이클만 연결
 */
public class PostgresContainerExtension implements BeforeAllCallback, AfterAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        PostgresTestContainer.start();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        PostgresTestContainer.stop();
    }
}
