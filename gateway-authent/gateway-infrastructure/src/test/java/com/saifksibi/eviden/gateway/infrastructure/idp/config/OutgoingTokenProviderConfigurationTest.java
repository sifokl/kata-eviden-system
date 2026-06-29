package com.saifksibi.eviden.gateway.infrastructure.idp.config;

import com.saifksibi.eviden.gateway.domain.port.OutgoingTokenProviderPort;
import com.saifksibi.eviden.gateway.infrastructure.idp.security.token.InternalJwtOutgoingTokenProvider;
import com.saifksibi.eviden.gateway.infrastructure.idp.security.token.OriginalTokenRelayProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OutgoingTokenProviderConfigurationTest {

    private final OutgoingTokenProviderConfiguration configuration =
            new OutgoingTokenProviderConfiguration();

    private final GatewayTokenProperties tokenProperties =
            mock(GatewayTokenProperties.class);

    private final OriginalTokenRelayProvider relayProvider =
            mock(OriginalTokenRelayProvider.class);

    private final InternalJwtOutgoingTokenProvider internalProvider =
            mock(InternalJwtOutgoingTokenProvider.class);

    @Test
    @DisplayName("Should select relay provider when propagation mode is RELAY")
    void shouldSelectRelayProviderWhenPropagationModeIsRelay() {

        // Arrange - configurer le mode RELAY
        when(tokenProperties.propagationTokenMode())
                .thenReturn(PropagationTokenMode.RELAY);

        // Act - demander au bean de sélectionner le provider
        OutgoingTokenProviderPort provider =
                configuration.outgoingTokenProviderPort(
                        tokenProperties,
                        relayProvider,
                        internalProvider
                );

        // Assert - vérifier que le provider RELAY est retourné
        assertSame(relayProvider, provider);
    }

    @Test
    @DisplayName("Should select internal provider when propagation mode is INTERNAL")
    void shouldSelectInternalProviderWhenPropagationModeIsInternal() {

        // Arrange - configurer le mode INTERNAL
        when(tokenProperties.propagationTokenMode())
                .thenReturn(PropagationTokenMode.INTERNAL);

        // Act - demander au bean de sélectionner le provider
        OutgoingTokenProviderPort provider =
                configuration.outgoingTokenProviderPort(
                        tokenProperties,
                        relayProvider,
                        internalProvider
                );

        // Assert - vérifier que le provider INTERNAL est retourné
        assertSame(internalProvider, provider);
    }
}