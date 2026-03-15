package com.devkor.ifive.nadab.global.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "oauth.google")
public class GoogleProperties {
    private String clientId;           // 웹용 Client ID
    private String androidClientId;    // Android용 Client ID
    private String clientSecret;
    private String redirectUri;
}