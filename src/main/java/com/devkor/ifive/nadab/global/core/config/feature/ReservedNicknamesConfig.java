package com.devkor.ifive.nadab.global.core.config.feature;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "reserved-nicknames")
public class ReservedNicknamesConfig {

    private List<String> nicknames;
}
