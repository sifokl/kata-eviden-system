package com.saifksibi.eviden.gateway.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenTest {

    private static final Instant ISSUED_AT = Instant.parse("2026-06-28T10:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2026-06-28T10:05:00Z");

    @Test
    @DisplayName("Should create valid JWT token")
    void shouldCreateValidJwtToken() {
        JwtToken token = validToken();

        assertEquals("jwt-token", token.value());
        assertEquals(TokenType.EXTERNAL_IDP_TOKEN, token.type());
        assertEquals("http://localhost:8180/realms/eviden-kata", token.issuer());
        assertEquals(Set.of("gateway-client"), token.audiences());
        assertEquals(ISSUED_AT, token.issuedAt());
        assertEquals(EXPIRES_AT, token.expiresAt());
    }

    @Test
    @DisplayName("Should reject blank token value")
    void shouldRejectBlankTokenValue() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new JwtToken(
                        "   ",
                        TokenType.EXTERNAL_IDP_TOKEN,
                        "issuer",
                        Set.of("gateway-client"),
                        ISSUED_AT,
                        EXPIRES_AT
                )
        );

        assertEquals("Token value must not be blank", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject empty audiences")
    void shouldRejectEmptyAudiences() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new JwtToken(
                        "jwt-token",
                        TokenType.EXTERNAL_IDP_TOKEN,
                        "issuer",
                        Set.of(),
                        ISSUED_AT,
                        EXPIRES_AT
                )
        );

        assertEquals("Token must contain at least one audience", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject issuedAt after expiresAt")
    void shouldRejectIssuedAtAfterExpiresAt() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new JwtToken(
                        "jwt-token",
                        TokenType.EXTERNAL_IDP_TOKEN,
                        "issuer",
                        Set.of("gateway-client"),
                        EXPIRES_AT,
                        ISSUED_AT
                )
        );

        assertEquals("Token issuedAt must be before expiresAt", exception.getMessage());
    }

    @Test
    @DisplayName("Should detect expired token")
    void shouldDetectExpiredToken() {
        JwtToken token = validToken();

        assertFalse(token.isExpiredAt(Instant.parse("2026-06-28T10:04:59Z")));
        assertTrue(token.isExpiredAt(Instant.parse("2026-06-28T10:05:00Z")));
        assertTrue(token.isExpiredAt(Instant.parse("2026-06-28T10:06:00Z")));
    }

    @Test
    @DisplayName("Should check audience and return bearer token")
    void shouldCheckAudienceAndReturnBearerToken() {
        JwtToken token = validToken();

        assertTrue(token.hasAudience("gateway-client"));
        assertFalse(token.hasAudience("other-client"));
        assertEquals("Bearer jwt-token", token.asBearerToken());
    }

    private JwtToken validToken() {
        return new JwtToken(
                " jwt-token ",
                TokenType.EXTERNAL_IDP_TOKEN,
                " http://localhost:8180/realms/eviden-kata ",
                Set.of("gateway-client"),
                ISSUED_AT,
                EXPIRES_AT
        );
    }
}