package com.saifksibi.eviden.gateway.infrastructure.idp.config;

import com.saifksibi.eviden.gateway.domain.port.OutgoingTokenProviderPort;
import com.saifksibi.eviden.gateway.infrastructure.idp.security.token.InternalJwtOutgoingTokenProvider;
import com.saifksibi.eviden.gateway.infrastructure.idp.security.token.OriginalTokenRelayProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class OutgoingTokenProviderConfiguration {

    @Bean
    @Primary
    public OutgoingTokenProviderPort outgoingTokenProviderPort(
            GatewayTokenProperties tokenProperties,
            OriginalTokenRelayProvider relayProvider, //re-envoie le meme jwt
            InternalJwtOutgoingTokenProvider internalProvider // regenere un internal jwt
    ) {
        return switch (tokenProperties.propagationTokenMode()) {
            case RELAY -> relayProvider;
            case INTERNAL -> internalProvider;
        };
    }
}