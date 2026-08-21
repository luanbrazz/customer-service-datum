package com.lb.scoremockservice.controller;

import com.lb.scoremockservice.dto.ScoreResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScoreController {

    @GetMapping("/scores/{cpf}")
    public ResponseEntity<?> getScore(@PathVariable String cpf) {

        if (cpf.endsWith("000")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\":\"score service temporarily unavailable\"}");
        }

        if (cpf.endsWith("111")) {
            sleep(6000);
        }

        if (cpf.endsWith("222")) {
            return ResponseEntity.ok("{\"cpf\":\"" + cpf + "\",\"status\":\"UNKNOWN\"}");
        }

        int score = 300 + (Math.abs(cpf.hashCode()) % 601);
        String classification = score >= 700 ? "LOW_RISK" : score >= 500 ? "MEDIUM_RISK" : "HIGH_RISK";
        return ResponseEntity.ok(new ScoreResponse(cpf, score, classification));
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
