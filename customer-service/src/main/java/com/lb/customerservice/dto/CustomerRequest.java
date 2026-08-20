package com.lb.customerservice.dto;

import com.lb.customerservice.domain.CustomerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.validator.constraints.br.CPF;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRequest {

    @NotBlank(message = "O nome e obrigatorio")
    @Pattern(regexp = "^[\\p{L} .'-]{2,120}$", message = "Nome invalido")
    private String name;

    @NotBlank(message = "O CPF e obrigatorio")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter exatamente 11 digitos numericos")
    @CPF(message = "CPF invalido")
    private String cpf;

    @NotBlank(message = "O e-mail e obrigatorio")
    @Email(message = "E-mail invalido")
    private String email;

    private CustomerStatus status;
}