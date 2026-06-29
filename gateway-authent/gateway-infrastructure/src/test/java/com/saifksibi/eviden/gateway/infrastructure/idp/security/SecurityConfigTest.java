package com.saifksibi.eviden.gateway.infrastructure.idp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saifksibi.eviden.gateway.infrastructure.idp.config.GatewaySecurityProperties;
import com.saifksibi.eviden.gateway.infrastructure.idp.handler.JsonCustomAccessDeniedHandler;
import com.saifksibi.eviden.gateway.infrastructure.idp.handler.JsonCustomAuthenticationEntryPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {
        SecurityConfig.class,
        SecurityConfigTest.TestBeans.class,
        SecurityConfigTest.TestController.class
})
@AutoConfigureMockMvc
@ActiveProfiles("security-test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should allow public endpoint without JWT")
    void shouldAllowPublicEndpointWithoutJwt() throws Exception {
        mockMvc.perform(get("/public/books"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should reject protected endpoint without JWT")
    void shouldRejectProtectedEndpointWithoutJwt() throws Exception {
        mockMvc.perform(get("/members/books"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should allow USER role on members endpoint")
    void shouldAllowUserRoleOnMembersEndpoint() throws Exception {
        mockMvc.perform(get("/members/books")
                        .with(jwt().authorities(() -> "ROLE_USER")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should reject USER role on admin endpoint")
    void shouldRejectUserRoleOnAdminEndpoint() throws Exception {
        mockMvc.perform(get("/admin/books")
                        .with(jwt().authorities(() -> "ROLE_USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow ADMIN role on admin endpoint")
    void shouldAllowAdminRoleOnAdminEndpoint() throws Exception {
        mockMvc.perform(get("/admin/books")
                        .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @TestConfiguration
    @EnableConfigurationProperties(GatewaySecurityProperties.class)
    static class TestBeans {

        @Bean(name = "mvcHandlerMappingIntrospector")
        HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
            return new HandlerMappingIntrospector();
        }

        @Bean
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter() {
            return new JwtGrantedAuthoritiesConverter();
        }

        @Bean
        JsonCustomAuthenticationEntryPoint authenticationEntryPoint() {
            return new JsonCustomAuthenticationEntryPoint(
                    new ObjectMapper().findAndRegisterModules()
            );
        }

        @Bean
        JsonCustomAccessDeniedHandler accessDeniedHandler() {
            return new JsonCustomAccessDeniedHandler(
                    new ObjectMapper().findAndRegisterModules()
            );
        }

        @Bean
        JwtDecoder jwtDecoder() {
            return mock(JwtDecoder.class);
        }
    }

    @RestController
    static class TestController {

        @GetMapping("/public/books")
        String publicBooks() {
            return "public";
        }

        @GetMapping("/members/books")
        String membersBooks() {
            return "members";
        }

        @GetMapping("/admin/books")
        String adminBooks() {
            return "admin";
        }
    }
}