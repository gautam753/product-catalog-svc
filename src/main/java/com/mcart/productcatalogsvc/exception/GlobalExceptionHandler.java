package com.mcart.productcatalogsvc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import com.mcart.productcatalogsvc.model.ErrorResponse;

import reactor.core.publisher.Mono;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Existing — NotFoundException (products, variants etc.)
    @ExceptionHandler(NotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleNotFound(
            NotFoundException ex,
            ServerWebExchange exchange) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), exchange);
    }

    // Fix: Added — OrderNotFoundException
    @ExceptionHandler(OrderNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleOrderNotFound(
            OrderNotFoundException ex,
            ServerWebExchange exchange) {
        return buildResponse(HttpStatus.NOT_FOUND, "Order Not Found", ex.getMessage(), exchange);
    }

    // Fix: Added — IllegalStateException (e.g. cancel shipped order, empty cart)
    @ExceptionHandler(IllegalStateException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalState(
            IllegalStateException ex,
            ServerWebExchange exchange) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), exchange);
    }

    // Fix: Added — IllegalArgumentException (e.g. invalid status, invalid UUID)
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalArgument(
            IllegalArgumentException ex,
            ServerWebExchange exchange) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), exchange);
    }

    // Fix: Added — Validation errors (@Valid @RequestBody failures)
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidation(
            WebExchangeBindException ex,
            ServerWebExchange exchange) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error", message, exchange);
    }

    // Generic catch-all
    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(
            RuntimeException ex,
            ServerWebExchange exchange) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred",
                exchange
        );
    }

    private Mono<ResponseEntity<ErrorResponse>> buildResponse(
            HttpStatus status, String error, String message, ServerWebExchange exchange) {

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(exchange.getRequest().getPath().value())
                .build();

        return Mono.just(ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body));
    }
}