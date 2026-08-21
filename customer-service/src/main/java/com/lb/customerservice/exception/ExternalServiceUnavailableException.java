package com.lb.customerservice.exception;

public class ExternalServiceUnavailableException extends RuntimeException {
    public ExternalServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}