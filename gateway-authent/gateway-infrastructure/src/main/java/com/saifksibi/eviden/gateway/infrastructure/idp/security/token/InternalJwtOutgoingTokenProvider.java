package com.saifksibi.eviden.gateway.infrastructure.idp.security.token;

import com.saifksibi.eviden.gateway.domain.model.JwtPrincipal;
import com.saifksibi.eviden.gateway.domain.model.JwtToken;
import com.saifksibi.eviden.gateway.domain.port.OutgoingTokenProviderPort;
import org.springframework.stereotype.Component;


@Component
public class InternalJwtOutgoingTokenProvider implements OutgoingTokenProviderPort {

    @Override
    public JwtToken provideTokenFor(JwtPrincipal principal) {

        // ICI on implemete le mecanisme de generation de token interne.
        // on peut pour l'instant reeutiliser l'ancien token (externe) , ou lever une exception
        //de type UnsupportedOperationException
        throw new UnsupportedOperationException(
                "Internal JWT generation is not implemented yet"
        );
    }
}