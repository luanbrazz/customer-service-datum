package com.lb.customerservice.mapper;

import com.lb.customerservice.domain.Customer;
import com.lb.customerservice.dto.CustomerRequest;
import com.lb.customerservice.dto.CustomerResponse;

public final class CustomerMapper {

    private CustomerMapper() {
    }

    public static Customer toEntity(CustomerRequest request) {
        return Customer.builder()
                .name(request.getName())
                .cpf(request.getCpf())
                .email(request.getEmail())
                .status(request.getStatus())
                .build();
    }

    public static CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .cpf(customer.getCpf())
                .email(customer.getEmail())
                .status(customer.getStatus())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}