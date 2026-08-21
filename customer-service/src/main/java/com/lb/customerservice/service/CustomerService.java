package com.lb.customerservice.service;

import com.lb.customerservice.domain.Customer;
import com.lb.customerservice.domain.CustomerStatus;
import com.lb.customerservice.dto.CustomerRequest;
import com.lb.customerservice.exception.CpfAlreadyExistsException;
import com.lb.customerservice.exception.CustomerNotFoundException;
import com.lb.customerservice.mapper.CustomerMapper;
import com.lb.customerservice.repository.CustomerJdbcRepository;
import com.lb.customerservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerJdbcRepository customerJdbcRepository;

    @Transactional
    public Customer create(CustomerRequest request) {
        validateCpfNotDuplicated(request.getCpf());

        Customer customer = CustomerMapper.toEntity(request);
        if (customer.getStatus() == null) {
            customer.setStatus(CustomerStatus.ACTIVE);
        }
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer update(Long id, CustomerRequest request) {
        Customer existing = findByIdOrThrow(id);

        if (!existing.getCpf().equals(request.getCpf())) {
            validateCpfNotDuplicated(request.getCpf());
        }

        applyUpdates(existing, request);
        return customerRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        customerRepository.delete(findByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Customer findById(Long id) {
        return findByIdOrThrow(id);
    }

    @Transactional(readOnly = true)
    public List<Customer> findAll(CustomerStatus status) {
        return status != null
                ? customerJdbcRepository.findByStatus(status)
                : customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Customer> searchByName(String name) {
        return customerRepository.searchByNameNative(name);
    }

    private void validateCpfNotDuplicated(String cpf) {
        if (customerRepository.existsByCpf(cpf)) {
            throw new CpfAlreadyExistsException(cpf);
        }
    }

    private void applyUpdates(Customer existing, CustomerRequest request) {
        existing.setName(request.getName());
        existing.setCpf(request.getCpf());
        existing.setEmail(request.getEmail());
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
    }

    private Customer findByIdOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }
}