package com.saifksibi.eviden.gateway.domain.exception;

public class InvalidJwtClaimsException extends RuntimeException {

    public InvalidJwtClaimsException(String message) {
        super(message);
    }

    public InvalidJwtClaimsException(String message, Throwable cause) {
        super(message, cause);
    }
}