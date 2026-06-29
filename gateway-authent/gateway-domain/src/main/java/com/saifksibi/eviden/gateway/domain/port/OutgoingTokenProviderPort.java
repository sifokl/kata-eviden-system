package com.saifksibi.eviden.gateway.domain.port;

import com.saifksibi.eviden.gateway.domain.model.JwtPrincipal;
import com.saifksibi.eviden.gateway.domain.model.JwtToken;

public interface OutgoingTokenProviderPort {

    JwtToken provideTokenFor(JwtPrincipal principal);
}