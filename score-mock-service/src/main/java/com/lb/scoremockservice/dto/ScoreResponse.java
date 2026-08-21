package com.lb.scoremockservice.dto;

public record ScoreResponse(String cpf, int score, String classification) {
}