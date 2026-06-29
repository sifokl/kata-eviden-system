package com.saifksibi.eviden.gateway.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtPrincipalTest {

    private static final Instant ISSUED_AT = Instant.parse("2026-06-28T10:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2026-06-28T10:05:00Z");

    @Test
    @DisplayName("Should create a valid JWT principal")
    void shouldCreateValidJwtPrincipal() {
        JwtPrincipal principal = validPrincipal();

        assertEquals("subject-123", principal.claims().subject());
        assertEquals("Bearer jwt-token", principal.sourceToken().asBearerToken());
        assertEquals(2, principal.roles().size());
    }

    @Test
    @DisplayName("Should return true when principal has expected role")
    void shouldReturnTrueWhenPrincipalHasRole() {
        JwtPrincipal principal = validPrincipal();

        assertTrue(principal.hasRole("ADMIN"));
        assertTrue(principal.hasRole("ROLE_ADMIN"));
        assertTrue(principal.hasRole("USER"));
        assertTrue(principal.hasRole("ROLE_USER"));
    }

    @Test
    @DisplayName("Should return false when principal does not have requested role")
    void shouldReturnFalseWhenPrincipalDoesNotHaveRole() {
        JwtPrincipal principal = validPrincipal();

        assertFalse(principal.hasRole("SUPER_ADMIN"));
        assertFalse(principal.hasRole("MANAGER"));
    }

    @Test
    @DisplayName("Should return true when principal has at least one requested role")
    void shouldReturnTrueWhenPrincipalHasAnyRequestedRole() {
        JwtPrincipal principal = validPrincipal();

        assertTrue(principal.hasAnyRole("MANAGER", "ADMIN"));
        assertTrue(principal.hasAnyRole("ROLE_USER"));
    }

    @Test
    @DisplayName("Should return false when principal has none of requested roles")
    void shouldReturnFalseWhenPrincipalHasAnyRequestedRole() {
        JwtPrincipal principal = validPrincipal();

        assertFalse(principal.hasAnyRole("MANAGER", "SUPER_ADMIN"));
    }

    @Test
    @DisplayName("Should delegate display name to JWT claims")
    void shouldDelegateDisplayNameToJwtClaims() {
        JwtPrincipal principal = validPrincipal();

        assertEquals("admin", principal.displayName());
    }


    private JwtPrincipal validPrincipal() {

        JwtClaims claims = new JwtClaims(
                "subject-123",
                "http://localhost:8180/realms/eviden-kata",
                Set.of("gateway-client"),
                "admin",
                "admin@eviden-kata.local",
                "Admin",
                "User",
                ISSUED_AT,
                EXPIRES_AT,
                Map.of()
        );

        JwtToken token = new JwtToken(
                "jwt-token",
                TokenType.EXTERNAL_IDP_TOKEN,
                "http://localhost:8180/realms/eviden-kata",
                Set.of("gateway-client"),
                ISSUED_AT,
                EXPIRES_AT
        );

        return new JwtPrincipal(
                claims,
                Set.of(
                        new Role("USER"),
                        new Role("ADMIN")
                ),
                token
        );
    }
}