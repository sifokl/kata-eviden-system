package com.saifksibi.eviden.gateway.launcher;

import com.saifksibi.eviden.gateway.application.principal.service.CurrentPrincipalService;
import com.saifksibi.eviden.gateway.application.proxy.service.ProxyBackendService;
import com.saifksibi.eviden.gateway.domain.port.BackendApiPort;
import com.saifksibi.eviden.gateway.domain.port.CurrentPrincipalProviderPort;
import com.saifksibi.eviden.gateway.domain.port.OutgoingTokenProviderPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationServiceConfiguration {

    @Bean
    public CurrentPrincipalService currentPrincipalService(
            CurrentPrincipalProviderPort currentPrincipalProvider
    ) {
        return new CurrentPrincipalService(currentPrincipalProvider);
    }

    @Bean
    public ProxyBackendService proxyBackendService(
            CurrentPrincipalProviderPort currentPrincipalProvider,
            OutgoingTokenProviderPort outgoingTokenProvider,
            BackendApiPort backendApiPort
    ) {
        return new ProxyBackendService(
                currentPrincipalProvider,
                outgoingTokenProvider,
                backendApiPort
        );
    }
}