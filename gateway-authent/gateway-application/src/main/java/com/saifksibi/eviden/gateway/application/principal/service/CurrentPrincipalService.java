package com.saifksibi.eviden.gateway.application.principal.service;

import com.saifksibi.eviden.gateway.application.principal.dto.JwtPrincipalResponseDTO;
import com.saifksibi.eviden.gateway.domain.model.JwtPrincipal;
import com.saifksibi.eviden.gateway.domain.port.CurrentPrincipalProviderPort;

import java.util.Objects;

public class CurrentPrincipalService {

    private final CurrentPrincipalProviderPort currentPrincipalProvider;

    public CurrentPrincipalService(CurrentPrincipalProviderPort currentPrincipalProvider) {
        this.currentPrincipalProvider = Objects.requireNonNull(
                currentPrincipalProvider,
                "CurrentPrincipalProviderPort must not be null"
        );
    }

    public JwtPrincipalResponseDTO getCurrentPrincipal() {
        JwtPrincipal principal = currentPrincipalProvider.getCurrentPrincipal();
        return JwtPrincipalResponseDTO.from(principal);
    }
}