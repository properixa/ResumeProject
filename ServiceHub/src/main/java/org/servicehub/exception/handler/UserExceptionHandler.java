package org.servicehub.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.servicehub.dto.ErrorResponse;
import org.servicehub.exception.exception.DuplicateEmailException;
import org.servicehub.exception.exception.InvalidRoleException;
import org.servicehub.exception.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> {
            if (!errors.containsKey(e.getField())) {
                errors.put(e.getField(), e.getDefaultMessage());
            }
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(
                        409,
                        "Duplicated emails",
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRole(InvalidRoleException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Invalid role",
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }
}
