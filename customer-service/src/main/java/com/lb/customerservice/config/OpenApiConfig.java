package com.lb.customerservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

@OpenAPIDefinition(
        info = @Info(
                title = "Customer Service API",
                version = "1.0",
                description = "Microsservico de gerenciamento de clientes - Desafio Tecnico Datum"
        )
)
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic"
)
public class OpenApiConfig {
}