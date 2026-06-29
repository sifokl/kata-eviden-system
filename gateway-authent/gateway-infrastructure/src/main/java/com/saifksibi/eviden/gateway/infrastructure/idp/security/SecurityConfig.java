package com.saifksibi.eviden.gateway.infrastructure.idp.security;

import com.saifksibi.eviden.gateway.infrastructure.idp.config.AccessRule;
import com.saifksibi.eviden.gateway.infrastructure.idp.config.GatewaySecurityProperties;
import com.saifksibi.eviden.gateway.infrastructure.idp.handler.JsonCustomAccessDeniedHandler;
import com.saifksibi.eviden.gateway.infrastructure.idp.handler.JsonCustomAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final GatewaySecurityProperties securityProperties;
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;
    private final JsonCustomAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonCustomAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(
            GatewaySecurityProperties securityProperties,
            JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter,
            JsonCustomAuthenticationEntryPoint authenticationEntryPoint, JsonCustomAccessDeniedHandler accessDeniedHandler) {
        this.securityProperties = securityProperties;
        this.jwtGrantedAuthoritiesConverter = jwtGrantedAuthoritiesConverter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // use our custom converter
        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);



        http
                .csrf(csrf -> csrf.disable())
                /**  first version : hard coded classical way to manage routes : BAD **/
                /*
                .authorizeHttpRequests(auth -> {
                    securityProperties.publicEndpoints()
                            .forEach(endpoint -> auth.requestMatchers(endpoint).permitAll());

                    auth
                            .requestMatchers("/api/me").authenticated()
                            .requestMatchers("/proxy/books").hasAnyRole("USER", "ADMIN")
                            .requestMatchers("/api/admin").hasRole("ADMIN")
                            .anyRequest().denyAll();
                })
                  */

                /** final version:  Dynamic Way to manage routes , from yaml file config , NO HARDCODED values **/
                .authorizeHttpRequests(auth -> {
                    securityProperties.publicEndpoints()
                            .forEach(endpoint -> auth.requestMatchers(endpoint).permitAll()  );
                    securityProperties.routeAuthorizations()
                            .forEach(route -> applyRouteAuthorization(auth, route) );
                    auth.anyRequest().denyAll();
                })
                /** fin gestion de access routes **/
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter))
                );

        return http.build();
    }

    private void applyRouteAuthorization(
            org.springframework.security.config.annotation.web.configurers
                    .AuthorizeHttpRequestsConfigurer<HttpSecurity>
                    .AuthorizationManagerRequestMatcherRegistry auth,
            GatewaySecurityProperties.RouteAuthorization route
    ) {
        AccessRule access = route.access();

        switch (access) {
            case AUTHENTICATED ->
                    auth.requestMatchers(route.pattern()).authenticated();
            case HAS_ROLE ->
                    auth.requestMatchers(route.pattern())
                            .hasRole(singleRole(route));
            case HAS_ANY_ROLE ->
                    auth.requestMatchers(route.pattern())
                            .hasAnyRole(rolesAsArray(route));
        }
    }

    private String singleRole(GatewaySecurityProperties.RouteAuthorization route) {
        List<String> roles = route.roles();

        if (roles == null || roles.size() != 1) {
            throw new IllegalArgumentException(
                    "Route " + route.pattern() + " with HAS_ROLE must define exactly one role"
            );
        }

        return roles.getFirst();
    }

    private String[] rolesAsArray(GatewaySecurityProperties.RouteAuthorization route) {
        List<String> roles = route.roles();

        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException(
                    "Route " + route.pattern() + " with HAS_ANY_ROLE must define at least one role"
            );
        }

        return roles.toArray(String[]::new);
    }


}