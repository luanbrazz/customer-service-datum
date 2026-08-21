package com.lb.customerservice.service;

import com.lb.customerservice.client.ScoreClient;
import com.lb.customerservice.domain.Customer;
import com.lb.customerservice.dto.ScoreResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final ScoreClient scoreClient;
    private final CustomerService customerService;

    public ScoreResponse getScoreByCustomerId(Long customerId) {
        Customer customer = customerService.findById(customerId);
        return scoreClient.fetchScore(customer.getCpf());
    }
}