package com.saifksibi.eviden.gateway.application.principal.service;

import com.saifksibi.eviden.gateway.application.principal.dto.JwtPrincipalResponseDTO;
import com.saifksibi.eviden.gateway.domain.exception.PrincipalNotFoundException;
import com.saifksibi.eviden.gateway.domain.model.*;
import com.saifksibi.eviden.gateway.domain.port.CurrentPrincipalProviderPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrentPrincipalServiceTest {

    private static final Instant ISSUED_AT = Instant.parse("2026-06-28T10:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2026-06-28T10:05:00Z");

    private final CurrentPrincipalProviderPort currentPrincipalProvider =
            mock(CurrentPrincipalProviderPort.class);

    private final CurrentPrincipalService service =
            new CurrentPrincipalService(currentPrincipalProvider);

    @Test
    @DisplayName("Should return current authenticated principal")
    void shouldReturnCurrentAuthenticatedPrincipal() {

        // Arrange - mocker le provider pour retourner un principal authentifié
        JwtPrincipal principal = validPrincipal();
        when(currentPrincipalProvider.getCurrentPrincipal()).thenReturn(principal);

        // Act - appeler le use case à tester
        JwtPrincipalResponseDTO response = service.getCurrentPrincipal();

        // Assert - vérifier que le DTO est correctement construit
        assertNotNull(response);
        assertEquals("subject-123", response.subject());
        assertEquals("admin", response.preferredUsername());
        assertTrue(response.roles().contains("ADMIN"));
        assertTrue(response.roles().contains("USER"));

        // Assert - vérifier que le provider a bien été appelé une seule fois
        verify(currentPrincipalProvider).getCurrentPrincipal();
        verifyNoMoreInteractions(currentPrincipalProvider);
    }

    @Test
    @DisplayName("Should propagate provider exception")
    void shouldPropagateProviderException() {

        // Arrange - mocker le provider pour simuler l'absence de principal authentifié
        when(currentPrincipalProvider.getCurrentPrincipal())
                .thenThrow(new PrincipalNotFoundException("No authenticated principal found"));

        // Act & Assert - vérifier que l'exception est propagée par le service
        PrincipalNotFoundException exception = assertThrows(
                PrincipalNotFoundException.class,
                service::getCurrentPrincipal
        );

        // Assert - vérifier le message d'erreur retourné
        assertEquals("No authenticated principal found", exception.getMessage());

        // Assert - vérifier que le provider a bien été appelé
        verify(currentPrincipalProvider).getCurrentPrincipal();
        verifyNoMoreInteractions(currentPrincipalProvider);
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
                Set.of(new Role("USER"), new Role("ADMIN")),
                token
        );
    }
}