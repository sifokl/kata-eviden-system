package com.saifksibi.eviden.gateway.infrastructure.proxy.securedresource;

import com.saifksibi.eviden.gateway.domain.model.JwtToken;
import com.saifksibi.eviden.gateway.domain.model.TokenType;
import com.saifksibi.eviden.gateway.infrastructure.proxy.config.BackendProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;

class RestClientBackendApiAdapterTest {

    private static final Instant ISSUED_AT = Instant.parse("2026-06-28T10:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2026-06-28T10:05:00Z");

    private final RestClient.Builder restClientBuilder = RestClient.builder();

    private final MockRestServiceServer server =
            MockRestServiceServer.bindTo(restClientBuilder).build();

    private final BackendProperties backendProperties = new BackendProperties(
            "http://localhost:8082",
            new BackendProperties.Resource(
                    "/api/library/public/books",
                    "/api/library/members/books",
                    "/api/library/admin/books"
            )
    );

    private final RestClientBackendApiAdapter adapter =
            new RestClientBackendApiAdapter(
                    restClientBuilder
                            .baseUrl(backendProperties.baseUrl())
                            .build(),
                    backendProperties
            );

    @Test
    @DisplayName("Should call public backend resource without Authorization header")
    void shouldCallPublicBackendResourceWithoutAuthorizationHeader() {

        // Arrange - mocker l'endpoint public du backend factice
        server.expect(once(), requestTo("http://localhost:8082/api/library/public/books"))
                .andExpect(method(GET))
                .andRespond(withSuccess("public-books-response", MediaType.APPLICATION_JSON));

        // Act - appeler l'adapter sortant public
        String response = adapter.getPublicResource();

        // Assert - vérifier la réponse retournée par le backend
        assertEquals("public-books-response", response);

        // Assert - vérifier que l'appel HTTP attendu a bien été exécuté
        server.verify();
    }

    @Test
    @DisplayName("Should call members backend resource with Authorization header")
    void shouldCallMembersBackendResourceWithAuthorizationHeader() {

        // Arrange - préparer le token sortant à propager au backend
        JwtToken outgoingToken = validToken();

        // Arrange - mocker l'endpoint members du backend factice avec header Authorization
        server.expect(once(), requestTo("http://localhost:8082/api/library/members/books"))
                .andExpect(method(GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer jwt-token"))
                .andRespond(withSuccess("members-books-response", MediaType.APPLICATION_JSON));

        // Act - appeler l'adapter sortant protégé members
        String response = adapter.getMembersResource(outgoingToken);

        // Assert - vérifier la réponse retournée par le backend
        assertEquals("members-books-response", response);

        // Assert - vérifier que l'appel HTTP attendu a bien été exécuté
        server.verify();
    }

    @Test
    @DisplayName("Should call admin backend resource with Authorization header")
    void shouldCallAdminBackendResourceWithAuthorizationHeader() {

        // Arrange - préparer le token sortant à propager au backend
        JwtToken outgoingToken = validToken();

        // Arrange - mocker l'endpoint admin du backend factice avec header Authorization
        server.expect(once(), requestTo("http://localhost:8082/api/library/admin/books"))
                .andExpect(method(GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer jwt-token"))
                .andRespond(withSuccess("admin-books-response", MediaType.APPLICATION_JSON));

        // Act - appeler l'adapter sortant protégé admin
        String response = adapter.getAdminResource(outgoingToken);

        // Assert - vérifier la réponse retournée par le backend
        assertEquals("admin-books-response", response);

        // Assert - vérifier que l'appel HTTP attendu a bien été exécuté
        server.verify();
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