package com.saifksibi.eviden.gateway.infrastructure.idp.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonCustomAccessDeniedHandlerTest {

    private final JsonCustomAccessDeniedHandler handler =
            new JsonCustomAccessDeniedHandler(new ObjectMapper().findAndRegisterModules());

    @Test
    @DisplayName("Should return HTTP 403 Forbidden response")
    void shouldReturnHttp403ForbiddenResponse() throws Exception {

        // Arrange - simuler une requête authentifiée mais sans rôle suffisant
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/admin/books");

        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act - construire la réponse HTTP 403
        handler.handle(
                request,
                response,
                new AccessDeniedException("Access denied")
        );

        // Assert - vérifier le statut HTTP
        assertEquals(403, response.getStatus());

        // Assert - vérifier le type de contenu
        assertEquals("application/json", response.getContentType());

        // Assert - vérifier que le message métier est présent
        assertTrue(response.getContentAsString()
                .contains("Access denied"));
    }
}