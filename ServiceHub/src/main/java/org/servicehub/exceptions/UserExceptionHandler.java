package org.servicehub.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.servicehub.dto.ErrorResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class UserExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(404).contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(
                        404,
                        "Not found",
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }
}
