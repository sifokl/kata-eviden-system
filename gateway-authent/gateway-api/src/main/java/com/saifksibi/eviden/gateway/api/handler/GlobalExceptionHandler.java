package com.saifksibi.eviden.gateway.api.handler;


import com.saifksibi.eviden.gateway.application.error.dto.ErrorResponseDTO;
import com.saifksibi.eviden.gateway.domain.exception.InvalidJwtClaimsException;
import com.saifksibi.eviden.gateway.domain.exception.PrincipalNotFoundException;
import com.saifksibi.eviden.gateway.domain.exception.UnauthorizedProxyAccessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PrincipalNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handlePrincipalNotFound(
            PrincipalNotFoundException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);
    }

    @ExceptionHandler(UnauthorizedProxyAccessException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnauthorizedProxyAccess(
            UnauthorizedProxyAccessException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.FORBIDDEN, exception.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }


    @ExceptionHandler(InvalidJwtClaimsException.class)
    public ResponseEntity<ErrorResponseDTO>  handleInvalidJwtClaims(
            InvalidJwtClaimsException ex,
            HttpServletRequest request){
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(
            Exception exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected internal error", request);
    }

    private ResponseEntity<ErrorResponseDTO> build(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(status)
                .body(ErrorResponseDTO.of(
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        request.getRequestURI()
                ));
    }
}