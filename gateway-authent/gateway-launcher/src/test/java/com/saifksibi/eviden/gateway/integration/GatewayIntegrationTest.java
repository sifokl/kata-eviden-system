package com.saifksibi.eviden.gateway.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.saifksibi.eviden.gateway.launcher.AuthGatewayApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.http.HttpHeaders;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AuthGatewayApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GatewayIntegrationTest {

    static final WireMockServer wireMockServer = new WireMockServer(0);

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void registerWireMockProperties(DynamicPropertyRegistry registry) {
        wireMockServer.start();
        registry.add("wiremock.server.port", wireMockServer::port);
    }

    @AfterEach
    void resetWireMock() {
        wireMockServer.resetAll();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("Should return public books without JWT")
    void shouldReturnPublicBooksWithoutJwt() throws Exception {

        // Arrange - simuler le backend public
        wireMockServer.stubFor(WireMock.get(urlEqualTo("/api/library/public/books"))
                .willReturn(okJson("""
                        {"scope":"PUBLIC","books":["Clean Code","Effective Java"]}
                        """)));

        // Act & Assert - appeler la gateway sans JWT
        mockMvc.perform(get("/public/books"))
                .andExpect(status().isOk());

        // Assert - vérifier que le backend public a bien été appelé
        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/library/public/books")));
    }

    @Test
    @DisplayName("Should reject members books without JWT")
    void shouldRejectMembersBooksWithoutJwt() throws Exception {

        // Act & Assert - appeler un endpoint protégé sans JWT
        mockMvc.perform(get("/members/books"))
                .andExpect(status().isUnauthorized());

        // Assert - vérifier que le backend n'a jamais été appelé
        wireMockServer.verify(0, getRequestedFor(urlEqualTo("/api/library/members/books")));
    }

    @Test
    @DisplayName("Should return members books with USER JWT and propagate Authorization header")
    void shouldReturnMembersBooksWithUserJwtAndPropagateAuthorizationHeader() throws Exception {

        // Arrange - simuler le backend members
        wireMockServer.stubFor(WireMock.get(urlEqualTo("/api/library/members/books"))
                .willReturn(okJson("""
                    {"scope":"MEMBERS","books":["Domain-Driven Design","Spring Security in Action"]}
                    """)));

        // Act & Assert - appeler la gateway avec JWT USER
        mockMvc.perform(get("/members/books")
                .with(userJwt()))
                .andExpect(status().isOk());

        // Assert - vérifier que le backend members a été appelé avec un Bearer token
        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/library/members/books"))
                .withHeader(HttpHeaders.AUTHORIZATION, matching("Bearer .*")));
    }

    @Test
    @DisplayName("Should reject admin books with USER JWT")
    void shouldRejectAdminBooksWithUserJwt() throws Exception {

        // Act & Assert - appeler l'endpoint admin avec un JWT USER
        mockMvc.perform(get("/admin/books")
                        .with(userJwt()))
                .andExpect(status().isForbidden());

        // Assert - vérifier que le backend admin n'a jamais été appelé
        wireMockServer.verify(0, getRequestedFor(urlEqualTo("/api/library/admin/books")));
    }

    @Test
    @DisplayName("Should return admin books with ADMIN JWT and propagate Authorization header")
    void shouldReturnAdminBooksWithAdminJwtAndPropagateAuthorizationHeader() throws Exception {

        // Arrange - simuler le backend admin
        wireMockServer.stubFor(WireMock.get(urlEqualTo("/api/library/admin/books"))
                .willReturn(okJson("""
                    {"scope":"ADMIN","books":["Release It!","Building Microservices"]}
                    """)));

        // Act & Assert - appeler la gateway avec JWT ADMIN
        mockMvc.perform(get("/admin/books")
                        .with(adminJwt()))
                .andExpect(status().isOk());

        // Assert - vérifier que le backend admin a été appelé avec un Bearer token
        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/library/admin/books"))
                .withHeader(HttpHeaders.AUTHORIZATION, matching("Bearer .*")));
    }



    @Test
    @DisplayName("Should return current principal with USER JWT")
    void shouldReturnCurrentPrincipalWithUserJwt() throws Exception {

        // Act & Assert - appeler /api/me avec un JWT USER complet
        mockMvc.perform(get("/api/me")
                        .with(adminJwt()))
                .andExpect(status().isOk());
    }




    private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor userJwt() {
        return jwt()
                .jwt(jwt -> jwt
                        .tokenValue("user-access-token")
                        .subject("user-subject")
                        .issuer("http://localhost:8180/realms/eviden-kata")
                        .audience(java.util.List.of("gateway-client"))
                        .issuedAt(java.time.Instant.parse("2026-06-29T10:00:00Z"))
                        .expiresAt(java.time.Instant.parse("2026-06-29T10:05:00Z"))
                        .claim("preferred_username", "user")
                        .claim("email", "user@eviden-kata.local")
                        .claim("given_name", "Standard")
                        .claim("family_name", "User")
                        .claim("roles", java.util.List.of("USER")))
                .authorities(() -> "ROLE_USER");
    }

    private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor adminJwt() {
        return jwt()
                .jwt(jwt -> jwt
                        .tokenValue("admin-access-token")
                        .subject("admin-subject")
                        .issuer("http://localhost:8180/realms/eviden-kata")
                        .audience(java.util.List.of("gateway-client"))
                        .issuedAt(java.time.Instant.parse("2026-06-29T10:00:00Z"))
                        .expiresAt(java.time.Instant.parse("2026-06-29T10:05:00Z"))
                        .claim("preferred_username", "admin")
                        .claim("email", "admin@eviden-kata.local")
                        .claim("given_name", "Admin")
                        .claim("family_name", "User")
                        .claim("roles", java.util.List.of("USER", "ADMIN")))
                .authorities(() -> "ROLE_ADMIN");
    }
}