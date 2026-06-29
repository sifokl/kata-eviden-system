package com.saifksibi.eviden.gateway.application.principal.dto;

import com.saifksibi.eviden.gateway.domain.model.JwtPrincipal;
import com.saifksibi.eviden.gateway.domain.model.Role;

import java.util.Set;
import java.util.stream.Collectors;

public record JwtPrincipalResponseDTO(
        String subject,
        String issuer,
        Set<String> audiences,
        String preferredUsername,
        String email,
        String givenName,
        String familyName,
        Set<String> roles,
        String tokenType
) {

    public static JwtPrincipalResponseDTO from(JwtPrincipal principal) {
        return new JwtPrincipalResponseDTO(
                principal.claims().subject(),
                principal.claims().issuer(),
                principal.claims().audiences(),
                principal.claims().preferredUsername(),
                principal.claims().email(),
                principal.claims().givenName(),
                principal.claims().familyName(),
                principal.roles()
                        .stream()
                        .map(Role::name)
                        .collect(Collectors.toUnmodifiableSet()),
                principal.sourceToken().type().name()
        );
    }
}