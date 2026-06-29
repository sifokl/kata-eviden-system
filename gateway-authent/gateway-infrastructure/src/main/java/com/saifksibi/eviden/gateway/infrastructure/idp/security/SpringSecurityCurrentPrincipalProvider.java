package com.saifksibi.eviden.gateway.infrastructure.idp.security;

import com.saifksibi.eviden.gateway.domain.exception.PrincipalNotFoundException;
import com.saifksibi.eviden.gateway.domain.model.JwtClaims;
import com.saifksibi.eviden.gateway.domain.model.JwtPrincipal;
import com.saifksibi.eviden.gateway.domain.model.JwtToken;
import com.saifksibi.eviden.gateway.domain.model.Role;
import com.saifksibi.eviden.gateway.domain.model.TokenType;
import com.saifksibi.eviden.gateway.domain.port.CurrentPrincipalProviderPort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SpringSecurityCurrentPrincipalProvider implements CurrentPrincipalProviderPort {

    private static final String CLAIM_PREFERRED_USERNAME = "preferred_username";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_GIVEN_NAME = "given_name";
    private static final String CLAIM_FAMILY_NAME = "family_name";

    @Override
    public JwtPrincipal getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new PrincipalNotFoundException("No authenticated principal found in security context");
        }

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new PrincipalNotFoundException("Authenticated principal is not a JWT principal");
        }

        return toJwtPrincipal(jwt, authentication);
    }

    private JwtPrincipal toJwtPrincipal(Jwt jwt, Authentication authentication) {
        JwtClaims claims = toJwtClaims(jwt);
        JwtToken sourceToken = toJwtToken(jwt);
        Set<Role> roles = toRoles(authentication);

        return new JwtPrincipal(
                claims,
                roles,
                sourceToken
        );
    }

    private JwtClaims toJwtClaims(Jwt jwt) {

        String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "unknown-issuer";

        return new JwtClaims(
                jwt.getSubject(),
                issuer,
                Set.copyOf(jwt.getAudience()),
                jwt.getClaimAsString(CLAIM_PREFERRED_USERNAME),
                jwt.getClaimAsString(CLAIM_EMAIL),
                jwt.getClaimAsString(CLAIM_GIVEN_NAME),
                jwt.getClaimAsString(CLAIM_FAMILY_NAME),
                jwt.getIssuedAt(),
                jwt.getExpiresAt(),
                Map.copyOf(jwt.getClaims())
        );
    }

    private JwtToken toJwtToken(Jwt jwt) {
        return new JwtToken(
                jwt.getTokenValue(),
                TokenType.EXTERNAL_IDP_TOKEN,
                jwt.getIssuer().toString(),
                Set.copyOf(jwt.getAudience()),
                jwt.getIssuedAt(),
                jwt.getExpiresAt()
        );
    }

    private Set<Role> toRoles(Authentication authentication) {
        Objects.requireNonNull(authentication, "Authentication must not be null");

        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(Role::new)
                .collect(Collectors.toUnmodifiableSet());
    }
}