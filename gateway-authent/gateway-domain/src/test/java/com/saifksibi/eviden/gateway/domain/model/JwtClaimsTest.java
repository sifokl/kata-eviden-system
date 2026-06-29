package com.saifksibi.eviden.gateway.domain.model;

import com.saifksibi.eviden.gateway.domain.exception.InvalidJwtClaimsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtClaimsTest {

    private static final Instant ISSUED_AT = Instant.parse("2026-06-28T10:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2026-06-28T10:05:00Z");

    @Test
    @DisplayName("Should create valid JWT claims")
    void shouldCreateValidJwtClaims() {
        JwtClaims claims = createValidClaims();

        assertEquals("subject-123", claims.subject());
        assertEquals("http://localhost:8180/realms/eviden-kata", claims.issuer());
        assertEquals(Set.of("gateway-client"), claims.audiences());
        assertEquals("admin", claims.preferredUsername());
        assertEquals("admin@eviden-kata.local", claims.email());
        assertEquals("Admin", claims.givenName());
        assertEquals("User", claims.familyName());
        assertEquals(ISSUED_AT, claims.issuedAt());
        assertEquals(EXPIRES_AT, claims.expiresAt());
    }

    @Test
    @DisplayName("Should reject null subject")
    void shouldRejectNullSubject() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JwtClaims(
                        null,
                        "issuer",
                        Set.of("gateway-client"),
                        "admin",
                        "admin@eviden-kata.local",
                        "Admin",
                        "User",
                        ISSUED_AT,
                        EXPIRES_AT,
                        Map.of()
                )
        );

        assertEquals("Subject must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject blank subject")
    void shouldRejectBlankSubject() {
        InvalidJwtClaimsException exception = assertThrows(
                InvalidJwtClaimsException.class,
                () -> new JwtClaims(
                        "   ",
                        "issuer",
                        Set.of("gateway-client"),
                        "admin",
                        "admin@eviden-kata.local",
                        "Admin",
                        "User",
                        ISSUED_AT,
                        EXPIRES_AT,
                        Map.of()
                )
        );

        assertEquals("JWT subject claim is missing or blank", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject null issuer")
    void shouldRejectNullIssuer() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JwtClaims(
                        "subject-123",
                        null,
                        Set.of("gateway-client"),
                        "admin",
                        "admin@eviden-kata.local",
                        "Admin",
                        "User",
                        ISSUED_AT,
                        EXPIRES_AT,
                        Map.of()
                )
        );

        assertEquals("Issuer must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject blank issuer")
    void shouldRejectBlankIssuer() {
        InvalidJwtClaimsException exception = assertThrows(
                InvalidJwtClaimsException.class,
                () -> new JwtClaims(
                        "subject-123",
                        "   ",
                        Set.of("gateway-client"),
                        "admin",
                        "admin@eviden-kata.local",
                        "Admin",
                        "User",
                        ISSUED_AT,
                        EXPIRES_AT,
                        Map.of()
                )
        );

        assertEquals("JWT Issuer must not be blank", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject null audiences")
    void shouldRejectNullAudiences() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JwtClaims(
                        "subject-123",
                        "issuer",
                        null,
                        "admin",
                        "admin@eviden-kata.local",
                        "Admin",
                        "User",
                        ISSUED_AT,
                        EXPIRES_AT,
                        Map.of()
                )
        );

        assertEquals("Audiences must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject empty audiences")
    void shouldRejectEmptyAudiences() {
        InvalidJwtClaimsException exception = assertThrows(
                InvalidJwtClaimsException.class,
                () -> new JwtClaims(
                        "subject-123",
                        "issuer",
                        Set.of(),
                        "admin",
                        "admin@eviden-kata.local",
                        "Admin",
                        "User",
                        ISSUED_AT,
                        EXPIRES_AT,
                        Map.of()
                )
        );

        assertEquals("JWT Audiences must not be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject blank audience")
    void shouldRejectBlankAudience() {
        InvalidJwtClaimsException exception = assertThrows(
                InvalidJwtClaimsException.class,
                () -> new JwtClaims(
                        "subject-123",
                        "issuer",
                        Set.of("gateway-client", " "),
                        "admin",
                        "admin@eviden-kata.local",
                        "Admin",
                        "User",
                        ISSUED_AT,
                        EXPIRES_AT,
                        Map.of()
                )
        );

        assertEquals("JWT Audiences must not contain blank values", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject null additional claims")
    void shouldRejectNullAdditionalClaims() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new JwtClaims(
                        "subject-123",
                        "issuer",
                        Set.of("gateway-client"),
                        "admin",
                        "admin@eviden-kata.local",
                        "Admin",
                        "User",
                        ISSUED_AT,
                        EXPIRES_AT,
                        null
                )
        );

        assertEquals("Additional claims must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should return additional claim by name")
    void shouldReturnAdditionalClaimByName() {
        JwtClaims claims = createValidClaims();

        assertEquals("custom-value", claims.claim("custom_claim"));
    }

    @Test
    @DisplayName("Should return null when additional claim does not exist")
    void shouldReturnNullWhenAdditionalClaimDoesNotExist() {
        JwtClaims claims = createValidClaims();

        assertNull(claims.claim("missing_claim"));
    }

    @Test
    @DisplayName("Should reject null claim name")
    void shouldRejectNullClaimName() {
        JwtClaims claims = createValidClaims();

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> claims.claim(null)
        );

        assertEquals("Claim name must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should use preferred username as display name")
    void shouldUsePreferredUsernameAsDisplayName() {
        JwtClaims claims = createValidClaims();

        assertEquals("admin", claims.displayName());
    }

    @Test
    @DisplayName("Should use email as display name when preferred username is missing")
    void shouldUseEmailAsDisplayNameWhenPreferredUsernameIsMissing() {
        JwtClaims claims = new JwtClaims(
                "subject-123",
                "issuer",
                Set.of("gateway-client"),
                null,
                "admin@eviden-kata.local",
                "Admin",
                "User",
                ISSUED_AT,
                EXPIRES_AT,
                Map.of()
        );

        assertEquals("admin@eviden-kata.local", claims.displayName());
    }

    @Test
    @DisplayName("Should use full name as display name when username and email are missing")
    void shouldUseFullNameAsDisplayNameWhenUsernameAndEmailAreMissing() {
        JwtClaims claims = new JwtClaims(
                "subject-123",
                "issuer",
                Set.of("gateway-client"),
                null,
                null,
                "Admin",
                "User",
                ISSUED_AT,
                EXPIRES_AT,
                Map.of()
        );

        assertEquals("Admin User", claims.displayName());
    }

    private JwtClaims createValidClaims() {
        return new JwtClaims(
                "subject-123",
                "http://localhost:8180/realms/eviden-kata",
                Set.of("gateway-client"),
                "admin",
                "admin@eviden-kata.local",
                "Admin",
                "User",
                ISSUED_AT,
                EXPIRES_AT,
                Map.of(
                        "custom_claim", "custom-value",
                        "roles", Set.of("USER", "ADMIN")
                )
        );
    }

}