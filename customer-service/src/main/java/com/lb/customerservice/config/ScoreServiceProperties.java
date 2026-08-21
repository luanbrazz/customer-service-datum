package com.lb.customerservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.score-service")
public class ScoreServiceProperties {
    private String baseUrl;
    private int connectTimeoutMs = 2000;
    private int readTimeoutMs = 3000;
}