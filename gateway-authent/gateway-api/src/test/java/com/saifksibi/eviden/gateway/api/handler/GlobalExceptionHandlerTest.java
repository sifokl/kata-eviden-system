package com.saifksibi.eviden.gateway.api.handler;

import com.saifksibi.eviden.gateway.application.error.dto.ErrorResponseDTO;
import com.saifksibi.eviden.gateway.domain.exception.InvalidJwtClaimsException;
import com.saifksibi.eviden.gateway.domain.exception.PrincipalNotFoundException;
import com.saifksibi.eviden.gateway.domain.exception.UnauthorizedProxyAccessException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should return 400 for IllegalArgumentException")
    void shouldReturn400ForIllegalArgumentException() {

        // Arrange - simuler une requête HTTP
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");

        // Act - appeler le handler
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleIllegalArgument(
                        new IllegalArgumentException("Invalid request parameter"),
                        request);

        // Assert - vérifier le code HTTP
        assertEquals(400, response.getStatusCode().value());

        // Assert - vérifier le contenu de la réponse
        assertNotNull(response.getBody());
        assertEquals("Invalid request parameter", response.getBody().message());
        assertEquals("/api/test", response.getBody().path());
    }

    @Test
    @DisplayName("Should return 400 for InvalidJwtClaimsException")
    void shouldReturn400ForInvalidJwtClaimsException() {

        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");

        // Act
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleInvalidJwtClaims(
                        new InvalidJwtClaimsException("Invalid JWT claims"),
                        request);

        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Invalid JWT claims", response.getBody().message());
        assertEquals("/api/test", response.getBody().path());
    }

    @Test
    @DisplayName("Should return 401 for PrincipalNotFoundException")
    void shouldReturn401ForPrincipalNotFoundException() {

        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");

        // Act
        ResponseEntity<ErrorResponseDTO> response =
                handler.handlePrincipalNotFound(
                        new PrincipalNotFoundException("No authenticated principal found"),
                        request);

        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("No authenticated principal found", response.getBody().message());
        assertEquals("/api/test", response.getBody().path());
    }

    @Test
    @DisplayName("Should return 403 for UnauthorizedProxyAccessException")
    void shouldReturn403ForUnauthorizedProxyAccessException() {

        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");

        // Act
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleUnauthorizedProxyAccess(
                        new UnauthorizedProxyAccessException("Access denied"),
                        request);

        // Assert
        assertEquals(403, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Access denied", response.getBody().message());
        assertEquals("/api/test", response.getBody().path());
    }

    @Test
    @DisplayName("Should return 500 for unexpected exception")
    void shouldReturn500ForUnexpectedException() {

        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");

        // Act
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleGeneric(
                        new RuntimeException("Database unavailable"),
                        request);

        // Assert
        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Unexpected internal error", response.getBody().message());
        assertEquals("/api/test", response.getBody().path());
    }
}