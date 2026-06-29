package com.saifksibi.eviden.gateway.infrastructure.idp.security;

import com.saifksibi.eviden.gateway.domain.exception.PrincipalNotFoundException;
import com.saifksibi.eviden.gateway.domain.model.JwtPrincipal;
import com.saifksibi.eviden.gateway.domain.model.TokenType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SpringSecurityCurrentPrincipalProviderTest {

    private static final Instant ISSUED_AT = Instant.parse("2026-06-28T10:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2026-06-28T10:05:00Z");

    private final SpringSecurityCurrentPrincipalProvider provider =
            new SpringSecurityCurrentPrincipalProvider();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return JWT principal from Spring Security context")
    void shouldReturnJwtPrincipalFromSpringSecurityContext() {

        // Arrange - créer un JWT valide comme celui reçu depuis Keycloak
        Jwt jwt = validJwt();

        // Arrange - placer une authentification JWT dans le SecurityContext
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                jwt,
                null,
                List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                )
        );
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act - récupérer le principal courant via l'adapter Spring Security
        JwtPrincipal principal = provider.getCurrentPrincipal();

        // Assert - vérifier les claims principaux extraits du JWT
        assertEquals("subject-123", principal.claims().subject());
        assertEquals("admin", principal.claims().preferredUsername());
        assertEquals("admin@eviden-kata.local", principal.claims().email());

        // Assert - vérifier les rôles Spring convertis vers le domaine
        assertTrue(principal.hasRole("USER"));
        assertTrue(principal.hasRole("ADMIN"));

        // Assert - vérifier le token source extrait du JWT
        assertEquals("jwt-token", principal.sourceToken().value());
        assertEquals(TokenType.EXTERNAL_IDP_TOKEN, principal.sourceToken().type());
    }

    @Test
    @DisplayName("Should reject missing authentication")
    void shouldRejectMissingAuthentication() {

        // Arrange - vider le SecurityContext pour simuler une requête non authentifiée
        SecurityContextHolder.clearContext();

        // Act & Assert - vérifier que l'absence d'authentification est refusée
        PrincipalNotFoundException exception = assertThrows(
                PrincipalNotFoundException.class,
                provider::getCurrentPrincipal
        );

        // Assert - vérifier le message d'erreur
        assertEquals(
                "No authenticated principal found in security context",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Should reject non JWT principal")
    void shouldRejectNonJwtPrincipal() {

        // Arrange - placer une authentification dont le principal n'est pas un JWT
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                "simple-user",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act & Assert - vérifier qu'un principal non JWT est refusé
        PrincipalNotFoundException exception = assertThrows(
                PrincipalNotFoundException.class,
                provider::getCurrentPrincipal
        );

        // Assert - vérifier le message d'erreur
        assertEquals(
                "Authenticated principal is not a JWT principal",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Should ignore authorities without ROLE prefix")
    void shouldIgnoreAuthoritiesWithoutRolePrefix() {

        // Arrange - créer un JWT valide
        Jwt jwt = validJwt();

        // Arrange - placer des authorities mixtes dans le SecurityContext
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                jwt,
                null,
                List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("SCOPE_openid"),
                        new SimpleGrantedAuthority("ADMIN")
                )
        );
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act - récupérer le principal courant
        JwtPrincipal principal = provider.getCurrentPrincipal();

        // Assert - vérifier que seuls les rôles préfixés ROLE_ sont conservés
        assertTrue(principal.hasRole("USER"));
        assertFalse(principal.hasRole("ADMIN"));
        assertEquals(1, principal.roles().size());
    }

    private Jwt validJwt() {
        return new Jwt(
                "jwt-token",
                ISSUED_AT,
                EXPIRES_AT,
                Map.of("alg", "RS256"),
                Map.of(
                        "sub", "subject-123",
                        "iss", "http://localhost:8180/realms/eviden-kata",
                        "aud", List.of("gateway-client"),
                        "preferred_username", "admin",
                        "email", "admin@eviden-kata.local",
                        "given_name", "Admin",
                        "family_name", "User",
                        "roles", List.of("USER", "ADMIN")
                )
        );
    }
}