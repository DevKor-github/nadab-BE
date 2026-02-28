package com.devkor.ifive.nadab.global.core.config.infra;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 스프링 @Async 기본 실행기 (경고 제거용 기본값)
     * - 특별히 지정 안 하면 이 풀로 감
     */
    @Bean(name = "taskExecutor")
    @Primary
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("async-default-");
        executor.initialize();
        return executor;
    }

    /**
     * 이메일 전용 실행기
     */
    @Bean
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(10);
        executor.initialize();
        return executor;
    }

    /**
     * 주간 리포트(LLM 호출) 전용 실행기
     * - LLM은 외부 I/O라 동시성 높게 주되, 무한 큐 적체는 막기 위해 큐 제한
     */
    @Bean(name = "weeklyReportTaskExecutor")
    public ThreadPoolTaskExecutor weeklyReportTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("async-weekly-report-");
        executor.initialize();
        return executor;
    }

    /**
     * 월간 리포트(LLM 호출) 전용 실행기
     * - LLM은 외부 I/O라 동시성 높게 주되, 무한 큐 적체는 막기 위해 큐 제한
     */
    @Bean(name = "monthlyReportTaskExecutor")
    public ThreadPoolTaskExecutor monthlyReportTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("async-monthly-report-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "typeReportTaskExecutor")
    public Executor typeReportTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-type-report-");
        executor.initialize();
        return executor;
    }

    /**
     * FCM 푸시 알림 전용 실행기
     * - FCM은 외부 I/O라 동시성 높게 주되, 무한 큐 적체는 막기 위해 큐 제한
     */
    @Bean(name = "fcmTaskExecutor")
    public Executor fcmTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-fcm-");
        executor.initialize();
        return executor;
    }
}