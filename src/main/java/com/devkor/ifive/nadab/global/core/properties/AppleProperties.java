package com.devkor.ifive.nadab.global.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "oauth.apple")
public class AppleProperties {
    private String clientId;
    private String teamId;
    private String keyId;
    private String privateKey;
}