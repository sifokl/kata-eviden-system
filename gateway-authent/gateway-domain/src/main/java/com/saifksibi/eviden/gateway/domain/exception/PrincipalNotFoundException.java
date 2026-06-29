package com.saifksibi.eviden.gateway.domain.exception;

public class PrincipalNotFoundException extends RuntimeException {

    public PrincipalNotFoundException(String message) {
        super(message);
    }
}