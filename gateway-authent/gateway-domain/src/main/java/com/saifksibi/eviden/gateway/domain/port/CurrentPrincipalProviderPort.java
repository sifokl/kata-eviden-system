package com.saifksibi.eviden.gateway.domain.port;

import com.saifksibi.eviden.gateway.domain.model.JwtPrincipal;

public interface CurrentPrincipalProviderPort {

    JwtPrincipal getCurrentPrincipal();
}