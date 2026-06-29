package com.saifksibi.eviden.gateway.domain.port;

import com.saifksibi.eviden.gateway.domain.model.JwtPrincipal;

public interface TokenValidatorPort {

    JwtPrincipal validate(String rawJwtToken);
}