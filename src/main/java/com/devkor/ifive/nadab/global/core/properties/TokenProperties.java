package com.devkor.ifive.nadab.global.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "token")
public class TokenProperties {
    private String jwtSecret;
    private Long accessTokenExpiration;
    private Long refreshTokenExpiration;
}