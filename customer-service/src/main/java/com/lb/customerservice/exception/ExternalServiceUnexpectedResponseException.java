package com.lb.customerservice.exception;

public class ExternalServiceUnexpectedResponseException extends RuntimeException {
    public ExternalServiceUnexpectedResponseException(String message) {
        super(message);
    }

    public ExternalServiceUnexpectedResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}