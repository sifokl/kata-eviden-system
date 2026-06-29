package com.saifksibi.eviden.gateway.infrastructure.idp.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "gateway.security")
public record GatewaySecurityProperties(

        @NotEmpty
        List<@NotBlank String> publicEndpoints,

        @Valid
        List<RouteAuthorization> routeAuthorizations

) {

    public record RouteAuthorization(

            @NotBlank
            String pattern,

            @NotNull
            AccessRule access, //if HAS_ROLE is extracted from yml properties , converted auto to AccessRule.HAS_ROLE

            List<String> roles
    ) {
    }
}