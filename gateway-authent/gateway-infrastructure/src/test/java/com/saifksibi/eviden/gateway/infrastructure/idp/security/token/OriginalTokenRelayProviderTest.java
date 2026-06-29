package com.saifksibi.eviden.gateway.infrastructure.idp.security.token;

import com.saifksibi.eviden.gateway.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OriginalTokenRelayProviderTest {

    private static final Instant ISSUED_AT = Instant.parse("2026-06-28T10:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2026-06-28T10:05:00Z");

    private final OriginalTokenRelayProvider provider = new OriginalTokenRelayProvider();

    @Test
    @DisplayName("Should return original source token from principal")
    void shouldReturnOriginalSourceTokenFromPrincipal() {

        // Arrange - créer un principal contenant le token externe reçu depuis l'IdP
        JwtPrincipal principal = validPrincipal();
        JwtToken sourceToken = principal.sourceToken();

        // Act - demander au provider quel token doit être propagé au backend
        JwtToken outgoingToken = provider.provideTokenFor(principal);

        // Assert - vérifier que le token propagé est exactement le token source
        assertSame(sourceToken, outgoingToken);
        assertEquals("Bearer jwt-token", outgoingToken.asBearerToken());
        assertEquals(TokenType.EXTERNAL_IDP_TOKEN, outgoingToken.type());
    }

    @Test
    @DisplayName("Should reject null principal")
    void shouldRejectNullPrincipal() {

        // Act & Assert - vérifier qu'un principal null est refusé
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> provider.provideTokenFor(null)
        );

        // Assert - vérifier le message d'erreur
        assertEquals("Principal must not be null", exception.getMessage());
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

        JwtToken sourceToken = new JwtToken(
                "jwt-token",
                TokenType.EXTERNAL_IDP_TOKEN,
                "http://localhost:8180/realms/eviden-kata",
                Set.of("gateway-client"),
                ISSUED_AT,
                EXPIRES_AT
        );

        return new JwtPrincipal(
                claims,
                Set.of(new Role("USER"), new Role("ADMIN")),
                sourceToken
        );
    }
}