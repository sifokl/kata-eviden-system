package com.saifksibi.eviden.gateway.domain.model;

import java.util.Objects;
import java.util.Set;

public record JwtPrincipal(
        JwtClaims claims,
        Set<Role> roles,
        JwtToken sourceToken
) {

    public JwtPrincipal {
        Objects.requireNonNull(claims, "Claims must not be null");
        Objects.requireNonNull(roles, "Roles must not be null");
        Objects.requireNonNull(sourceToken, "Source token must not be null");

        roles = Set.copyOf(roles);
    }

    public boolean hasRole(String roleName) {
        Objects.requireNonNull(roleName, "Role name must not be null");

        return roles.stream()
                .anyMatch(role -> role.matches(roleName));
    }

    public boolean hasAnyRole(String... roleNames) {
        Objects.requireNonNull(roleNames, "Role names must not be null");

        for (String roleName : roleNames) {
            if (hasRole(roleName)) {
                return true;
            }
        }

        return false;
    }

    public Object claim(String claimName) {
        Objects.requireNonNull(claimName, "Claim name must not be null");
        return claims.claim(claimName);
    }

    public String displayName() {
        return claims.displayName();
    }
}