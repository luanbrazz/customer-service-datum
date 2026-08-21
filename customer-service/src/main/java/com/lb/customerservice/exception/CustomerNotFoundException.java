package com.lb.customerservice.exception;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(Long id) {
        super("Cliente nao encontrado para o id: " + id);
    }
}