package com.saifksibi.eviden.gateway.application.proxy.service;

import com.saifksibi.eviden.gateway.application.proxy.dto.BackendResourceResponseDTO;
import com.saifksibi.eviden.gateway.domain.model.*;
import com.saifksibi.eviden.gateway.domain.port.BackendApiPort;
import com.saifksibi.eviden.gateway.domain.port.CurrentPrincipalProviderPort;
import com.saifksibi.eviden.gateway.domain.port.OutgoingTokenProviderPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProxyBackendServiceTest {

    private static final Instant ISSUED_AT = Instant.parse("2026-06-28T10:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2026-06-28T10:05:00Z");

    private final CurrentPrincipalProviderPort currentPrincipalProvider = mock(CurrentPrincipalProviderPort.class);
    private final OutgoingTokenProviderPort outgoingTokenProvider = mock(OutgoingTokenProviderPort.class);
    private final BackendApiPort backendApiPort = mock(BackendApiPort.class);

    private final ProxyBackendService service = new ProxyBackendService(
            currentPrincipalProvider,
            outgoingTokenProvider,
            backendApiPort
    );

    @Test
    @DisplayName("Should get public books without principal and outgoing token")
    void shouldGetPublicBooksWithoutPrincipalAndOutgoingToken() {

        // Arrange - mocker le backend pour retourner une réponse publique
        when(backendApiPort.getPublicResource()).thenReturn("public-books-response");

        // Act - appeler le use case à tester
        BackendResourceResponseDTO response = service.getPublicBooks();

        // Assert - vérifier qu'une réponse est retournée
        assertNotNull(response);

        // Assert - vérifier que seul le backend est appelé
        verify(backendApiPort).getPublicResource();

        // Assert - vérifier qu'aucun principal ni token sortant n'est utilisé
        verifyNoInteractions(currentPrincipalProvider);
        verifyNoInteractions(outgoingTokenProvider);
    }

    @Test
    @DisplayName("Should get members books using current principal and outgoing token")
    void shouldGetMembersBooksUsingCurrentPrincipalAndOutgoingToken() {

        JwtPrincipal principal = validPrincipal();
        JwtToken outgoingToken = validToken();

        // Arrange - mocker le principal authentifié
        when(currentPrincipalProvider.getCurrentPrincipal()).thenReturn(principal);

        // Arrange - mocker la génération (ou propagation) du JWT sortant
        when(outgoingTokenProvider.provideTokenFor(principal)).thenReturn(outgoingToken);

        // Arrange - mocker la réponse du backend
        when(backendApiPort.getMembersResource(outgoingToken))
                .thenReturn("members-books-response");

        // Act - appeler le use case à tester
        BackendResourceResponseDTO response = service.getMembersBooks();

        // Assert - vérifier qu'une réponse est retournée
        assertNotNull(response);

        // Assert - vérifier l'ordre logique des appels
        verify(currentPrincipalProvider).getCurrentPrincipal();
        verify(outgoingTokenProvider).provideTokenFor(principal);
        verify(backendApiPort).getMembersResource(outgoingToken);
    }

    @Test
    @DisplayName("Should get admin books using current principal and outgoing token")
    void shouldGetAdminBooksUsingCurrentPrincipalAndOutgoingToken() {

        JwtPrincipal principal = validPrincipal();
        JwtToken outgoingToken = validToken();

        // Arrange - mocker le principal authentifié
        when(currentPrincipalProvider.getCurrentPrincipal()).thenReturn(principal);

        // Arrange - mocker la génération (ou propagation) du JWT sortant
        when(outgoingTokenProvider.provideTokenFor(principal)).thenReturn(outgoingToken);

        // Arrange - mocker la réponse du backend
        when(backendApiPort.getAdminResource(outgoingToken))
                .thenReturn("admin-books-response");

        // Act - appeler le use case à tester
        BackendResourceResponseDTO response = service.getAdminBooks();

        // Assert - vérifier qu'une réponse est retournée
        assertNotNull(response);

        // Assert - vérifier que toutes les dépendances ont été sollicitées
        verify(currentPrincipalProvider).getCurrentPrincipal();
        verify(outgoingTokenProvider).provideTokenFor(principal);
        verify(backendApiPort).getAdminResource(outgoingToken);
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

        return new JwtPrincipal(
                claims,
                Set.of(new Role("USER"), new Role("ADMIN")),
                validToken()
        );
    }

    private JwtToken validToken() {
        return new JwtToken(
                "jwt-token",
                TokenType.EXTERNAL_IDP_TOKEN,
                "http://localhost:8180/realms/eviden-kata",
                Set.of("gateway-client"),
                ISSUED_AT,
                EXPIRES_AT
        );
    }
}