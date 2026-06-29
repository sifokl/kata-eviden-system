package com.saifksibi.eviden.gateway.infrastructure.proxy.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "gateway.backend")
public record BackendProperties(
        @NotBlank String baseUrl,
        @Valid Resource resource // ici books
) {
        public record Resource(
                @NotBlank String publicPath,
                @NotBlank String membersPath,
                @NotBlank String adminPath
        ) {
        }
}