package org.servicehub.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.servicehub.dto.error.ErrorResponse;
import org.servicehub.exception.exception.security.PrincipalException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class SecurityExceptionHandler {
    @ExceptionHandler(PrincipalException.class)
    public ResponseEntity<ErrorResponse> handlePrincipalException(PrincipalException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Unauthorized",
                    ex.getMessage(),
                    request.getRequestURI()
                ));
    }
}
