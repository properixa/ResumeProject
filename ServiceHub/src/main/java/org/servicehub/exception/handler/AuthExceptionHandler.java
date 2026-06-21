package org.servicehub.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.servicehub.dto.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AuthExceptionHandler {
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFound(UsernameNotFoundException ex,
                                                                HttpServletRequest request) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "User with this login not found",
                        ex.getMessage(),
                        request.getRequestURI()
                )
        );
    }
}
