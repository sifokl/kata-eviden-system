package com.saifksibi.eviden.gateway.api.controller;

import com.saifksibi.eviden.gateway.application.principal.dto.JwtPrincipalResponseDTO;
import com.saifksibi.eviden.gateway.application.principal.service.CurrentPrincipalService;
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

import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeController.class)
class MeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrentPrincipalService currentPrincipalService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("Should reject /api/me without JWT")
    void shouldRejectApiMeWithoutJwt() throws Exception {

        // Act & Assert - appeler /api/me sans JWT
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());

        // Assert - vérifier que le service applicatif n'est pas appelé
        verifyNoInteractions(currentPrincipalService);
    }

    @Test
    @DisplayName("Should return current principal with USER JWT")
    void shouldReturnCurrentPrincipalWithUserJwt() throws Exception {

        // Arrange - mocker le service applicatif pour retourner un principal USER
        when(currentPrincipalService.getCurrentPrincipal())
                .thenReturn(userPrincipal());

        // Act & Assert - appeler /api/me avec un JWT USER
        mockMvc.perform(get("/api/me")
                        .with(jwt().authorities(() -> "ROLE_USER")))
                .andExpect(status().isOk());

        // Assert - vérifier que le controller délègue au service applicatif
        verify(currentPrincipalService).getCurrentPrincipal();
        verifyNoMoreInteractions(currentPrincipalService);
    }

    @Test
    @DisplayName("Should return current principal with ADMIN JWT")
    void shouldReturnCurrentPrincipalWithAdminJwt() throws Exception {

        // Arrange - mocker le service applicatif pour retourner un principal ADMIN
        when(currentPrincipalService.getCurrentPrincipal())
                .thenReturn(adminPrincipal());

        // Act & Assert - appeler /api/me avec un JWT ADMIN
        mockMvc.perform(get("/api/me")
                        .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk());

        // Assert - vérifier que le controller délègue au service applicatif
        verify(currentPrincipalService).getCurrentPrincipal();
        verifyNoMoreInteractions(currentPrincipalService);
    }

    private JwtPrincipalResponseDTO userPrincipal() {
        return new JwtPrincipalResponseDTO(
                "subject-user",
                "http://localhost:8180/realms/eviden-kata",
                Set.of("gateway-client"),
                "user",
                "user@eviden-kata.local",
                "Standard",
                "User",
                Set.of("USER"),
                "EXTERNAL_IDP_TOKEN"
        );
    }

    private JwtPrincipalResponseDTO adminPrincipal() {
        return new JwtPrincipalResponseDTO(
                "subject-admin",
                "http://localhost:8180/realms/eviden-kata",
                Set.of("gateway-client"),
                "admin",
                "admin@eviden-kata.local",
                "Admin",
                "User",
                Set.of("USER", "ADMIN"),
                "EXTERNAL_IDP_TOKEN"
        );
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