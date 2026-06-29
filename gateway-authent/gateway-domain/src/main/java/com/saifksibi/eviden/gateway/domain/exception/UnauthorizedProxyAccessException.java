package com.saifksibi.eviden.gateway.domain.exception;

public class UnauthorizedProxyAccessException extends RuntimeException {

    public UnauthorizedProxyAccessException(String message) {
        super(message);
    }
}