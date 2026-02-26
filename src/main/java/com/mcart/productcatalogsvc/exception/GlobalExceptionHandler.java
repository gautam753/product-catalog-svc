package com.mcart.productcatalogsvc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebExchange;

import com.mcart.productcatalogsvc.model.ErrorResponse;

import reactor.core.publisher.Mono;

import java.time.Instant;

//@ControllerAdvice
public class GlobalExceptionHandler {

 @ExceptionHandler(NotFoundException.class)
 public Mono<ResponseEntity<ErrorResponse>> handleNotFound(
         NotFoundException ex,
         ServerWebExchange exchange) {

     ErrorResponse error = ErrorResponse.builder()
             .timestamp(Instant.now())
             .status(HttpStatus.NOT_FOUND.value())
             .error("Not Found")
             .message(ex.getMessage())
             .path(exchange.getRequest().getPath().value())
             .build();

     return Mono.just(ResponseEntity
             .status(HttpStatus.NOT_FOUND)
             .contentType(MediaType.APPLICATION_JSON)
             .body(error));
 }

 // Optional: Handle generic RuntimeException (catch-all)
 @ExceptionHandler(RuntimeException.class)
 public Mono<ResponseEntity<ErrorResponse>> handleGenericException(
         RuntimeException ex,
         ServerWebExchange exchange) {

     ErrorResponse error = ErrorResponse.builder()
             .timestamp(Instant.now())
             .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
             .error("Internal Server Error")
             .message("An unexpected error occurred")
             .path(exchange.getRequest().getPath().value())
             .build();

     return Mono.just(ResponseEntity
             .status(HttpStatus.INTERNAL_SERVER_ERROR)
             .contentType(MediaType.APPLICATION_JSON)
             .body(error));
 }
}