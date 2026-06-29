package com.saifksibi.eviden.gateway.infrastructure.idp.security.token;

import com.saifksibi.eviden.gateway.domain.model.JwtPrincipal;
import com.saifksibi.eviden.gateway.domain.model.JwtToken;
import com.saifksibi.eviden.gateway.domain.port.OutgoingTokenProviderPort;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class OriginalTokenRelayProvider implements OutgoingTokenProviderPort {

    @Override
    public JwtToken provideTokenFor(JwtPrincipal principal) {
        Objects.requireNonNull(principal, "Principal must not be null");
        return principal.sourceToken();
    }
}