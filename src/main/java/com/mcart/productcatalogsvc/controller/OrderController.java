package com.mcart.productcatalogsvc.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mcart.productcatalogsvc.orders.dto.CreateOrderRequest;
import com.mcart.productcatalogsvc.orders.dto.OrderResponse;
import com.mcart.productcatalogsvc.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ─── POST /orders ────────────────────────────────────────────────────────
    // Fix 1: Removed @ResponseStatus(CREATED) — conflicts with ResponseEntity
    // Fix 2: Added correct imports for @AuthenticationPrincipal and Jwt
    // Fix 3: Removed unused AddressSnapshot buildAddressSnapshot — now handled in service
    @PostMapping
    public Mono<ResponseEntity<OrderResponse>> createOrder(
    		@RequestHeader(value = "authoeized-user-id", required = false) String authorizedUserId,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        String userId = authorizedUserId;

        log.info("Creating order for user: {} with {} payment", userId, request.getPaymentMethod());

        return orderService.createOrder(userId, request)
                .map(order -> ResponseEntity.status(HttpStatus.CREATED).body(order));
    }

    // ─── GET /orders ─────────────────────────────────────────────────────────
    @GetMapping
    public Flux<OrderResponse> getMyOrders(@RequestHeader(value = "authoeized-user-id", required = false) String authorizedUserId) {
        String userId = authorizedUserId;
        log.debug("Fetching orders for user: {}", userId);
        return orderService.getOrdersByUserId(userId);
    }

    // ─── GET /orders/{orderId} ───────────────────────────────────────────────
    @GetMapping("/{orderId}")
    public Mono<ResponseEntity<OrderResponse>> getOrder(
    		@RequestHeader(value = "authoeized-user-id", required = false) String authorizedUserId,
            @PathVariable String orderId
    ) {
        String userId = authorizedUserId;
        return orderService.getOrderById(orderId, userId)
                .map(ResponseEntity::ok);
    }

    // ─── PATCH /orders/{orderId}/cancel ──────────────────────────────────────
    @PatchMapping("/{orderId}/cancel")
    public Mono<ResponseEntity<OrderResponse>> cancelOrder(
    		@RequestHeader(value = "authoeized-user-id", required = false) String authorizedUserId,
            @PathVariable String orderId
    ) {
        String userId = authorizedUserId;
        log.info("Cancelling order: {} for user: {}", orderId, userId);
        return orderService.cancelOrder(orderId, userId)
                .map(ResponseEntity::ok);
    }

    // ─── PATCH /orders/{orderId}/status (admin/internal) ─────────────────────
    @PatchMapping("/{orderId}/status")
    public Mono<ResponseEntity<OrderResponse>> updateStatus(
            @PathVariable String orderId,
            @RequestBody Map<String, String> body
    ) {
        String status = body.get("status");
        return orderService.updateStatus(orderId, status)
                .map(ResponseEntity::ok);
    }
}