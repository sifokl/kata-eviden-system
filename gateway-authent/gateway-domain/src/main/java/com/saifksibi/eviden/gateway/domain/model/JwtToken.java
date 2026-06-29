package com.saifksibi.eviden.gateway.domain.model;


import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public record JwtToken(
        String value,
        TokenType type,
        String issuer,
        Set<String> audiences,
        Instant issuedAt,
        Instant expiresAt
) {

    public JwtToken {
        Objects.requireNonNull(value, "Token value must not be null");
        Objects.requireNonNull(type, "Token type must not be null");
        Objects.requireNonNull(issuer, "Token issuer must not be null");
        Objects.requireNonNull(audiences, "Token audiences must not be null");
        Objects.requireNonNull(expiresAt, "Token expiration date must not be null");

        value = value.trim();
        issuer = issuer.trim();
        audiences = Set.copyOf(audiences);

        if (value.isBlank()) {
            throw new IllegalArgumentException("Token value must not be blank");
        }

        if (issuer.isBlank()) {
            throw new IllegalArgumentException("Token issuer must not be blank");
        }

        if (audiences.isEmpty()) {
            throw new IllegalArgumentException("Token must contain at least one audience");
        }

        if (audiences.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException("Token audiences must not contain blank value");
        }

        if (issuedAt != null && issuedAt.isAfter(expiresAt)) {
            throw new IllegalArgumentException("Token issuedAt must be before expiresAt");
        }
    }

    public boolean isExpiredAt(Instant instant) {
        Objects.requireNonNull(instant, "Instant must not be null");
        return !expiresAt.isAfter(instant);
    }

    public boolean hasAudience(String expectedAudience) {
        Objects.requireNonNull(expectedAudience, "Expected audience must not be null");
        return audiences.contains(expectedAudience);
    }

    public String asBearerToken() {
        return "Bearer " + value;
    }
}