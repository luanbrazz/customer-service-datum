package com.lb.customerservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ScoreResponse(String cpf, Integer score, String classification) {
}