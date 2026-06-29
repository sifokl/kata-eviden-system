package com.saifksibi.eviden.gateway.infrastructure.idp.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


@Validated
@ConfigurationProperties(prefix ="gateway.token")
public record GatewayTokenProperties(@NotNull PropagationTokenMode propagationTokenMode) {}
