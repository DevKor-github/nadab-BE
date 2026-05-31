package com.devkor.ifive.nadab.domain.admin.core.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "admin.page")
public class AdminPageProperties {

    @NotBlank
    private String password;

    @Min(60)
    private long tokenExpirationSeconds = 43200;

    @NotBlank
    private String cookieName = "admin_auth";
}
