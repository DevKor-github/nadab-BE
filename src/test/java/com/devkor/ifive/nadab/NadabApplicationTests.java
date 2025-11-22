package com.devkor.ifive.nadab;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NadabApplicationTests {

    @Disabled("추후 Testcontainers 기반 통합 테스트로의 대체까지 비활성화")
	@Test
	void contextLoads() {
	}

}
