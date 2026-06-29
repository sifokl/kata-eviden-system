package com.saifksibi.eviden.gateway.infrastructure.proxy.securedresource;

import com.saifksibi.eviden.gateway.domain.model.JwtToken;
import com.saifksibi.eviden.gateway.domain.port.BackendApiPort;
import com.saifksibi.eviden.gateway.infrastructure.proxy.config.BackendProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Component
public class RestClientBackendApiAdapter implements BackendApiPort {

    private final RestClient backendRestClient;
    private final BackendProperties backendProperties;

    public RestClientBackendApiAdapter(
            RestClient backendRestClient,
            BackendProperties backendProperties
    ) {
        this.backendRestClient = Objects.requireNonNull(
                backendRestClient,
                "Backend RestClient must not be null"
        );
        this.backendProperties = Objects.requireNonNull(
                backendProperties,
                "Backend properties must not be null"
        );
    }

    @Override
    public String getPublicResource() {
        return backendRestClient.get()
                .uri(backendProperties.resource().publicPath())
                .retrieve()
                .body(String.class);
    }

    @Override
    public String getMembersResource(JwtToken outgoingToken) {
        return getProtectedResource(backendProperties.resource().membersPath(), outgoingToken);
    }

    @Override
    public String getAdminResource(JwtToken outgoingToken) {
        return getProtectedResource(backendProperties.resource().adminPath(), outgoingToken);
    }

    private String getProtectedResource(String path, JwtToken outgoingToken) {
        return backendRestClient.get()
                .uri(path)
                .header(HttpHeaders.AUTHORIZATION, outgoingToken.asBearerToken())
                .retrieve()
                .body(String.class);
    }
}