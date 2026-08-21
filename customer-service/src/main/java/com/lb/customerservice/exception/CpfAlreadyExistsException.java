package com.lb.customerservice.exception;

public class CpfAlreadyExistsException extends RuntimeException {
    public CpfAlreadyExistsException(String cpf) {
        super("Ja existe um cliente cadastrado com o CPF informado");
    }
}