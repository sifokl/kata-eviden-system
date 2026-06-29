package com.saifksibi.eviden.gateway.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.saifksibi.eviden.gateway.launcher.AuthGatewayApplication;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@SpringBootTest(classes = AuthGatewayApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class GatewayTestContainersIntegrationTest {


    static final KeycloakContainer keycloak =
            new KeycloakContainer("quay.io/keycloak/keycloak:26.0.7")
                    .withRealmImportFile("keycloak/realm-eviden-kata.json");

    @Autowired
    private MockMvc mockMvc;

    private static final WireMockServer wireMockServer = new WireMockServer(0);

    @AfterEach
    void resetWireMock() {
        wireMockServer.resetAll();
    }

    @AfterAll
    static void stop() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {

        if (!keycloak.isRunning()) {
            keycloak.start();
        }

        if (!wireMockServer.isRunning()) {
            wireMockServer.start();
        }

        registry.add(
                "spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloak.getAuthServerUrl() + "/realms/eviden-kata"
        );

        registry.add(
                "gateway.backend.base-url",
                wireMockServer::baseUrl
        );
    }

    @Test
    void shouldStartKeycloakContainer() {
        assertTrue(keycloak.isRunning());
    }

    @Test //smoke test N1 pour check keyclock est OK
    void shouldGetUserAccessTokenFromKeycloak() {
        String token = requestAccessToken("user", "user");

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test //smoke test N2 pour check keyclock est OK
    void shouldRejectInvalidCredentials() {

        // Arrange - préparer des identifiants invalides
        String username = "user";
        String invalidPassword = "wrong-password";

        // Act & Assert - demander un token avec un mauvais mot de passe doit échouer
        assertThrows(
                RestClientResponseException.class,
                () -> requestAccessToken(username, invalidPassword)
        );
    }
/*
    @Test
    @DisplayName("Should access members endpoint with real Keycloak USER token")
    void shouldAccessMembersEndpointWithRealKeycloakUserToken() throws Exception {

        // Arrange - récupérer un vrai access token USER depuis Keycloak
        String accessToken = requestAccessToken("user", "user");

        // Arrange - simuler le backend members
        wireMockServer.stubFor(WireMock.get(urlEqualTo("/api/library/members/books"))
                .willReturn(okJson("""
                    {"scope":"MEMBERS","books":["Domain-Driven Design","Spring Security in Action"]}
                    """)));

        // Act & Assert - appeler la Gateway avec un vrai JWT Keycloak
        mockMvc.perform(get("/members/books")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());

        // Assert - vérifier que la Gateway a relayé le vrai JWT au backend fake
        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/library/members/books"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + accessToken)));
    }
*/


    private String requestAccessToken(String username, String password) {
        RestClient restClient = RestClient.create();

        Map<String, Object> response = restClient.post()
                .uri(keycloak.getAuthServerUrl()
                        + "/realms/eviden-kata/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("grant_type=password"
                        + "&client_id=gateway-client"
                        + "&client_secret=gateway-secret"
                        + "&username=" + username
                        + "&password=" + password)
                .retrieve()
                .body(Map.class);

        assertNotNull(response);

        return (String) response.get("access_token");
    }
}