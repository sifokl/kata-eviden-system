package com.saifksibi.eviden.gateway.domain.port;

import com.saifksibi.eviden.gateway.domain.model.JwtToken;

public interface BackendApiPort {

    String getPublicResource();

    String getMembersResource(JwtToken outgoingToken);

    String getAdminResource(JwtToken outgoingToken);
}