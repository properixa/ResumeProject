package org.servicehub.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.servicehub.dto.error.ErrorResponse;
import org.servicehub.exception.exception.order.InvalidForServiceExecutorException;
import org.servicehub.exception.exception.order.OrderChangeStatusException;
import org.servicehub.exception.exception.order.OrderNotFoundException;
import org.servicehub.exception.exception.order.OrderStatusNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class OrderExceptionHandler {
    @ExceptionHandler(InvalidForServiceExecutorException.class)
    public ResponseEntity<ErrorResponse> handleInvalidForServiceExecutor(InvalidForServiceExecutorException ex,
                                                                         HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        "Invalid for Service Executor",
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        "Order not found",
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(OrderStatusNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderStatusNotFound(OrderStatusNotFoundException ex,
                                                                   HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(
                   HttpStatus.BAD_REQUEST.value(),
                   "Order status is invalid",
                   ex.getMessage(),
                   request.getRequestURI()
                ));
    }

    @ExceptionHandler(OrderChangeStatusException.class)
    public ResponseEntity<ErrorResponse> handleOrderChangeStatus(OrderChangeStatusException ex,
                                                                 HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Unavailable to change status",
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }
}
