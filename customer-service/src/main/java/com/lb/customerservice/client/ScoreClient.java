package com.lb.customerservice.client;

import com.lb.customerservice.config.ScoreServiceProperties;
import com.lb.customerservice.dto.ScoreResponse;
import com.lb.customerservice.exception.ExternalServiceTimeoutException;
import com.lb.customerservice.exception.ExternalServiceUnavailableException;
import com.lb.customerservice.exception.ExternalServiceUnexpectedResponseException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;

@Component
public class ScoreClient {

    private final RestTemplate restTemplate;
    private final ScoreServiceProperties properties;

    public ScoreClient(RestTemplate scoreRestTemplate, ScoreServiceProperties properties) {
        this.restTemplate = scoreRestTemplate;
        this.properties = properties;
    }

    public ScoreResponse fetchScore(String cpf) {
        String url = properties.getBaseUrl() + "/scores/{cpf}";
        try {
            ScoreResponse response = restTemplate.getForObject(url, ScoreResponse.class, cpf);

            if (response == null || response.score() == null || response.classification() == null) {
                throw new ExternalServiceUnexpectedResponseException(
                        "Resposta do servico de score veio vazia ou incompleta");
            }
            return response;

        } catch (HttpStatusCodeException ex) {
            HttpStatusCode statusCode = ex.getStatusCode();
            if (statusCode.is5xxServerError()) {
                throw new ExternalServiceUnavailableException("Servico de score respondeu com erro de servidor", ex);
            }
            throw new ExternalServiceUnexpectedResponseException(
                    "Servico de score respondeu com status inesperado: " + statusCode.value(), ex);

        } catch (ResourceAccessException ex) {
            if (isTimeout(ex)) {
                throw new ExternalServiceTimeoutException("Timeout ao consultar o servico de score", ex);
            }
            throw new ExternalServiceUnavailableException("Falha de comunicacao com o servico de score", ex);

        } catch (RestClientException ex) {
            throw new ExternalServiceUnexpectedResponseException("Erro inesperado ao consultar o servico de score", ex);
        }
    }

    private boolean isTimeout(ResourceAccessException ex) {
        return ex.getCause() instanceof SocketTimeoutException;
    }
}