package com.saifksibi.eviden.gateway.domain.model;

import com.saifksibi.eviden.gateway.domain.exception.InvalidJwtClaimsException;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record JwtClaims(
        String subject,
        String issuer,
        Set<String> audiences,
        String preferredUsername,
        String email,
        String givenName,
        String familyName,
        Instant issuedAt,
        Instant expiresAt,
        Map<String, Object> additionalClaims
) {

    public JwtClaims {
        Objects.requireNonNull(subject, "Subject must not be null");
        Objects.requireNonNull(issuer, "Issuer must not be null");
        Objects.requireNonNull(audiences, "Audiences must not be null");
        Objects.requireNonNull(additionalClaims, "Additional claims must not be null");

        subject = subject.trim();
        issuer = issuer.trim();
        preferredUsername = normalizeNullable(preferredUsername);
        email = normalizeNullable(email);
        givenName = normalizeNullable(givenName);
        familyName = normalizeNullable(familyName);

        audiences = Set.copyOf(audiences);
        additionalClaims = Map.copyOf(additionalClaims);

        if (subject.isBlank()) {
            throw new InvalidJwtClaimsException("JWT subject claim is missing or blank");
        }

        if (issuer.isBlank()) {
            throw new InvalidJwtClaimsException("JWT Issuer must not be blank");
        }

        if (audiences.isEmpty()) {
            throw new InvalidJwtClaimsException("JWT Audiences must not be empty");
        }

        if (audiences.stream().anyMatch(String::isBlank)) {
            throw new InvalidJwtClaimsException("JWT Audiences must not contain blank values");
        }
    }

    public Object claim(String claimName) {
        Objects.requireNonNull(claimName, "Claim name must not be null");
        return additionalClaims.get(claimName);
    }

    public String displayName() {
        if (preferredUsername != null) {
            return preferredUsername;
        }

        if (email != null) {
            return email;
        }

        if (givenName != null || familyName != null) {
            return "%s %s"
                    .formatted(
                            givenName == null ? "" : givenName,
                            familyName == null ? "" : familyName
                    )
                    .trim();
        }

        return subject;
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}