package com.lb.customerservice.controller;

import com.lb.customerservice.domain.Customer;
import com.lb.customerservice.domain.CustomerStatus;
import com.lb.customerservice.dto.CustomerRequest;
import com.lb.customerservice.dto.CustomerResponse;
import com.lb.customerservice.mapper.CustomerMapper;
import com.lb.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        Customer created = customerService.create(request);
        CustomerResponse response = CustomerMapper.toResponse(created);
        return ResponseEntity.created(URI.create("/customers/" + created.getId())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        Customer updated = customerService.update(id, request);
        return ResponseEntity.ok(CustomerMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> findById(@PathVariable Long id) {
        Customer customer = customerService.findById(id);
        return ResponseEntity.ok(CustomerMapper.toResponse(customer));
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> findAll(@RequestParam(required = false) CustomerStatus status) {
        List<CustomerResponse> response = customerService.findAll(status).stream()
                .map(CustomerMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CustomerResponse>> searchByName(@RequestParam String name) {
        List<CustomerResponse> response = customerService.searchByName(name).stream()
                .map(CustomerMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
}