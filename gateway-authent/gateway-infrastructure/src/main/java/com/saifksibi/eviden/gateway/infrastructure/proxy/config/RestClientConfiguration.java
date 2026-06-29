package com.saifksibi.eviden.gateway.infrastructure.proxy.config;

import com.saifksibi.eviden.gateway.infrastructure.idp.config.GatewaySecurityProperties;
import com.saifksibi.eviden.gateway.infrastructure.idp.config.GatewayTokenProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({
        BackendProperties.class,
        GatewaySecurityProperties.class,
        GatewayTokenProperties.class
})
public class RestClientConfiguration {

    @Bean
    public RestClient backendRestClient(
            RestClient.Builder builder,
            BackendProperties backendProperties
    ) {
        return builder
                .baseUrl(backendProperties.baseUrl())
                .build();
    }
}