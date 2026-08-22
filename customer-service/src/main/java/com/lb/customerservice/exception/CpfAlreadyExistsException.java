package com.lb.customerservice.exception;

public class CpfAlreadyExistsException extends RuntimeException {

    public CpfAlreadyExistsException(String cpf) {
        super("Ja existe um cliente cadastrado com o CPF informado");
    }

    public CpfAlreadyExistsException(String cpf, Long inactiveCustomerId) {
        super("Ja existe um cliente cadastrado com o CPF informado, porem ele esta INATIVO (id="
                + inactiveCustomerId + "). Reative-o em vez de criar um novo cliente, via "
                + "PATCH /customers/" + inactiveCustomerId + "/activate");
    }
}