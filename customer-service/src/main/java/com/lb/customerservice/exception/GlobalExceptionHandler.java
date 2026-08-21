package com.lb.customerservice.exception;

import com.lb.customerservice.dto.ApiErrorResponse;
import com.lb.customerservice.dto.FieldErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(CustomerNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(CpfAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleCpfConflict(CpfAlreadyExistsException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> FieldErrorDetail.builder()
                        .field(fe.getField())
                        .message(fe.getDefaultMessage())
                        .build())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Dados invalidos. Verifique os campos informados.", request, details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado. Contate o administrador.", request, null);
    }

    @ExceptionHandler(ExternalServiceUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleExternalUnavailable(ExternalServiceUnavailableException ex, HttpServletRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Servico de score esta indisponivel no momento. Tente novamente mais tarde.", request, null);
    }

    @ExceptionHandler(ExternalServiceTimeoutException.class)
    public ResponseEntity<ApiErrorResponse> handleExternalTimeout(ExternalServiceTimeoutException ex, HttpServletRequest request) {
        return build(HttpStatus.GATEWAY_TIMEOUT, "Tempo limite excedido ao consultar o servico de score.", request, null);
    }

    @ExceptionHandler(ExternalServiceUnexpectedResponseException.class)
    public ResponseEntity<ApiErrorResponse> handleExternalUnexpected(ExternalServiceUnexpectedResponseException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, "Resposta inesperada recebida do servico de score.", request, null);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, HttpServletRequest request,
                                                   List<FieldErrorDetail> errors) {
        ApiErrorResponse body = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .errors(errors)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}