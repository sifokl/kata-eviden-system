package com.saifksibi.eviden.gateway.api.controller;

import com.saifksibi.eviden.gateway.application.proxy.dto.BackendResourceResponseDTO;
import com.saifksibi.eviden.gateway.application.proxy.service.ProxyBackendService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResourceProxyController.class)
class ResourceProxyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProxyBackendService proxyBackendService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("Should return public books without JWT")
    void shouldReturnPublicBooksWithoutJwt() throws Exception {

        // Arrange - mocker la réponse du service public
        when(proxyBackendService.getPublicBooks())
                .thenReturn(response("public-books-response"));

        // Act & Assert - appeler l'endpoint public sans JWT
        mockMvc.perform(get("/public/books"))
                .andExpect(status().isOk());

        // Assert - vérifier que le controller délègue au bon service
        verify(proxyBackendService).getPublicBooks();
        verifyNoMoreInteractions(proxyBackendService);
    }

    @Test
    @DisplayName("Should reject members books without JWT")
    void shouldRejectMembersBooksWithoutJwt() throws Exception {

        // Act & Assert - appeler l'endpoint members sans JWT
        mockMvc.perform(get("/members/books"))
                .andExpect(status().isUnauthorized());

        // Assert - vérifier que le service applicatif n'est pas appelé
        verifyNoInteractions(proxyBackendService);
    }

    @Test
    @DisplayName("Should return members books with USER JWT")
    void shouldReturnMembersBooksWithUserJwt() throws Exception {

        // Arrange - mocker la réponse du service members
        when(proxyBackendService.getMembersBooks())
                .thenReturn(response("members-books-response"));

        // Act & Assert - appeler l'endpoint members avec ROLE_USER
        mockMvc.perform(get("/members/books")
                        .with(jwt().authorities(() -> "ROLE_USER")))
                .andExpect(status().isOk());

        // Assert - vérifier que le controller délègue au bon service
        verify(proxyBackendService).getMembersBooks();
        verifyNoMoreInteractions(proxyBackendService);
    }

    @Test
    @DisplayName("Should return members books with ADMIN JWT")
    void shouldReturnMembersBooksWithAdminJwt() throws Exception {

        // Arrange - mocker la réponse du service members
        when(proxyBackendService.getMembersBooks())
                .thenReturn(response("members-books-response"));

        // Act & Assert - appeler l'endpoint members avec ROLE_ADMIN
        mockMvc.perform(get("/members/books")
                        .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk());

        // Assert - vérifier que le controller délègue au bon service
        verify(proxyBackendService).getMembersBooks();
        verifyNoMoreInteractions(proxyBackendService);
    }

    @Test
    @DisplayName("Should reject admin books without JWT")
    void shouldRejectAdminBooksWithoutJwt() throws Exception {

        // Act & Assert - appeler l'endpoint admin sans JWT
        mockMvc.perform(get("/admin/books"))
                .andExpect(status().isUnauthorized());

        // Assert - vérifier que le service applicatif n'est pas appelé
        verifyNoInteractions(proxyBackendService);
    }

    @Test
    @DisplayName("Should reject admin books with USER JWT")
    void shouldRejectAdminBooksWithUserJwt() throws Exception {

        // Act & Assert - appeler l'endpoint admin avec ROLE_USER
        mockMvc.perform(get("/admin/books")
                        .with(jwt().authorities(() -> "ROLE_USER")))
                .andExpect(status().isForbidden());

        // Assert - vérifier que le service applicatif n'est pas appelé
        verifyNoInteractions(proxyBackendService);
    }

    @Test
    @DisplayName("Should return admin books with ADMIN JWT")
    void shouldReturnAdminBooksWithAdminJwt() throws Exception {

        // Arrange - mocker la réponse du service admin
        when(proxyBackendService.getAdminBooks())
                .thenReturn(response("admin-books-response"));

        // Act & Assert - appeler l'endpoint admin avec ROLE_ADMIN
        mockMvc.perform(get("/admin/books")
                        .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk());

        // Assert - vérifier que le controller délègue au bon service
        verify(proxyBackendService).getAdminBooks();
        verifyNoMoreInteractions(proxyBackendService);
    }

    private BackendResourceResponseDTO response(String content) {
        return new BackendResourceResponseDTO(content);
    }

    @SpringBootApplication
    static class TestApplication {
    }

    @TestConfiguration
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/public/books").permitAll()
                            .requestMatchers("/api/me").authenticated()
                            .requestMatchers("/members/books").hasAnyRole("USER", "ADMIN")
                            .requestMatchers("/admin/books").hasRole("ADMIN")
                            .anyRequest().denyAll()
                    )
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt())
                    .build();
        }
    }
}