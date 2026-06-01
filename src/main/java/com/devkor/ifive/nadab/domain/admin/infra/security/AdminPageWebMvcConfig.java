package com.devkor.ifive.nadab.domain.admin.infra.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class AdminPageWebMvcConfig implements WebMvcConfigurer {

    private final AdminPageAuthInterceptor adminPageAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminPageAuthInterceptor)
                .addPathPatterns("/admin/**");
    }
}
