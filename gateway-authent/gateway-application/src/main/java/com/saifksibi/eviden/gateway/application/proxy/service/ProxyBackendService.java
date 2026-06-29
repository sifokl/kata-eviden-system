package com.saifksibi.eviden.gateway.application.proxy.service;

import com.saifksibi.eviden.gateway.application.proxy.dto.BackendResourceResponseDTO;
import com.saifksibi.eviden.gateway.domain.model.JwtPrincipal;
import com.saifksibi.eviden.gateway.domain.model.JwtToken;
import com.saifksibi.eviden.gateway.domain.port.BackendApiPort;
import com.saifksibi.eviden.gateway.domain.port.CurrentPrincipalProviderPort;
import com.saifksibi.eviden.gateway.domain.port.OutgoingTokenProviderPort;

import java.util.Objects;

public class ProxyBackendService {

    private final CurrentPrincipalProviderPort currentPrincipalProvider;
    private final OutgoingTokenProviderPort outgoingTokenProvider;
    private final BackendApiPort backendApiPort;

    public ProxyBackendService(
            CurrentPrincipalProviderPort currentPrincipalProvider,
            OutgoingTokenProviderPort outgoingTokenProvider,
            BackendApiPort backendApiPort
    ) {
        this.currentPrincipalProvider = Objects.requireNonNull(
                currentPrincipalProvider,
                "CurrentPrincipalProviderPort must not be null"
        );
        this.outgoingTokenProvider = Objects.requireNonNull(
                outgoingTokenProvider,
                "OutgoingTokenProviderPort must not be null"
        );
        this.backendApiPort = Objects.requireNonNull(
                backendApiPort,
                "BackendApiPort must not be null"
        );
    }

    public BackendResourceResponseDTO getPublicBooks() {
        return new BackendResourceResponseDTO(backendApiPort.getPublicResource());
    }

    public BackendResourceResponseDTO getMembersBooks() {
        JwtPrincipal principal = currentPrincipalProvider.getCurrentPrincipal();
        JwtToken outgoingToken = outgoingTokenProvider.provideTokenFor(principal);
        return new BackendResourceResponseDTO(backendApiPort.getMembersResource(outgoingToken));
    }

    public BackendResourceResponseDTO getAdminBooks() {
        JwtPrincipal principal = currentPrincipalProvider.getCurrentPrincipal();
        JwtToken outgoingToken = outgoingTokenProvider.provideTokenFor(principal);
        return new BackendResourceResponseDTO(backendApiPort.getAdminResource(outgoingToken));
    }
}