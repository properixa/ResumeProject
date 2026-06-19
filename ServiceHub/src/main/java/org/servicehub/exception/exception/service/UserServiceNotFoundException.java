package org.servicehub.exception.exception.service;

public class UserServiceNotFoundException extends RuntimeException {
    public UserServiceNotFoundException(String message) {
        super(message);
    }
}
