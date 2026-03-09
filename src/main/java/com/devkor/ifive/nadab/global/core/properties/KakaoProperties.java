package com.devkor.ifive.nadab.global.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "oauth.kakao")
public class KakaoProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private Integer appId;
}