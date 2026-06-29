package com.saifksibi.eviden.gateway.infrastructure.idp.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;


@Component
public class JwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String CLAIM_ROLES = "roles";



    /**
     * IMPORTANT : Converts the JWT "roles" ,  assume the following simple format (roles) :
     * { "sub": "...", "preferred_username": "...", "roles": ["USER", "ADMIN"] }
     */
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Object rolesClaim = jwt.getClaim(CLAIM_ROLES);

        if (!(rolesClaim instanceof Collection<?> roles)) {
            return Set.of();
        }

        return roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(String::trim)
                .filter(role -> !role.isBlank())
                .map(this::toSpringRole) // add prefix ROLE_
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet()); // unchangeable set of roles
    }

    private String toSpringRole(String role) {
        return role.startsWith(ROLE_PREFIX)
                ? role
                : ROLE_PREFIX + role;
    }
}