package com.saifksibi.eviden.gateway.infrastructure.idp.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtGrantedAuthoritiesConverterTest {

    private final JwtGrantedAuthoritiesConverter converter =
            new JwtGrantedAuthoritiesConverter();

    @Test
    @DisplayName("Should convert roles claim to Spring Security authorities")
    void shouldConvertRolesClaimToSpringSecurityAuthorities() {

        // Arrange - créer un JWT contenant les rôles simples exposés par Keycloak
        Jwt jwt = jwtWithClaims(Map.of(
                "roles", List.of("USER", "ADMIN")
        ));

        // Act - convertir les rôles JWT en GrantedAuthority Spring Security
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Assert - vérifier que les rôles sont préfixés avec ROLE_
        assertEquals(2, authorities.size());
        assertTrue(containsAuthority(authorities, "ROLE_USER"));
        assertTrue(containsAuthority(authorities, "ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Should keep role prefix when already present")
    void shouldKeepRolePrefixWhenAlreadyPresent() {

        // Arrange - créer un JWT avec un rôle déjà préfixé
        Jwt jwt = jwtWithClaims(Map.of(
                "roles", List.of("ROLE_ADMIN")
        ));

        // Act - convertir les rôles JWT
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Assert - vérifier que le préfixe ROLE_ n'est pas dupliqué
        assertEquals(1, authorities.size());
        assertTrue(containsAuthority(authorities, "ROLE_ADMIN"));
        assertFalse(containsAuthority(authorities, "ROLE_ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Should return empty authorities when roles claim is missing")
    void shouldReturnEmptyAuthoritiesWhenRolesClaimIsMissing() {

        // Arrange - créer un JWT sans claim roles
        Jwt jwt = jwtWithClaims(Map.of(
                "preferred_username", "admin"
        ));

        // Act - convertir les rôles JWT
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Assert - vérifier qu'aucune autorité n'est créée
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }

    @Test
    @DisplayName("Should ignore blank and non-string roles")
    void shouldIgnoreBlankAndNonStringRoles() {

        // Arrange - créer un JWT avec des rôles invalides et un rôle valide
        Jwt jwt = jwtWithClaims(Map.of(
                "roles", List.of("USER", "   ", 123, true)
        ));

        // Act - convertir les rôles JWT
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Assert - vérifier que seuls les rôles String non vides sont conservés
        assertEquals(1, authorities.size());
        assertTrue(containsAuthority(authorities, "ROLE_USER"));
    }

    private Jwt jwtWithClaims(Map<String, Object> claims) {
        return new Jwt(
                "jwt-token",
                Instant.parse("2026-06-28T10:00:00Z"),
                Instant.parse("2026-06-28T10:05:00Z"),
                Map.of("alg", "RS256"),
                claims
        );
    }

    private boolean containsAuthority(
            Collection<GrantedAuthority> authorities,
            String expectedAuthority
    ) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(expectedAuthority::equals);
    }
}