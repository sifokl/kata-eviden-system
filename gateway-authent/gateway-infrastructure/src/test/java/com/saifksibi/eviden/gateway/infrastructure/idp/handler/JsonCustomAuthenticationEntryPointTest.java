package com.saifksibi.eviden.gateway.infrastructure.idp.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonCustomAuthenticationEntryPointTest {

    private final JsonCustomAuthenticationEntryPoint entryPoint =
            new JsonCustomAuthenticationEntryPoint(new ObjectMapper().findAndRegisterModules());

    @Test
    @DisplayName("Should return HTTP 401 Unauthorized response")
    void shouldReturnHttp401UnauthorizedResponse() throws Exception {

        // Arrange - simuler une requête non authentifiée
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/members/books");

        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act - construire la réponse HTTP 401
        entryPoint.commence(
                request,
                response,
                new BadCredentialsException("Invalid token")
        );

        // Assert - vérifier le statut HTTP
        assertEquals(401, response.getStatus());

        // Assert - vérifier le type de contenu
        assertEquals("application/json", response.getContentType());

        // Assert - vérifier que le message métier est présent
        assertTrue(response.getContentAsString()
                .contains("Missing, expired or invalid authentication token"));
    }
}