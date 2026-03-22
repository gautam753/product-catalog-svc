package com.mcart.productcatalogsvc.service;

import com.mcart.productcatalogsvc.entity.UserAddress;
import com.mcart.productcatalogsvc.exception.OrderNotFoundException;
import com.mcart.productcatalogsvc.orders.dto.CreateOrderRequest;
import com.mcart.productcatalogsvc.orders.dto.OrderResponse;
import com.mcart.productcatalogsvc.orders.model.AddressSnapshot;
import com.mcart.productcatalogsvc.orders.model.Order;
import com.mcart.productcatalogsvc.orders.model.OrderItem;
import com.mcart.productcatalogsvc.orders.model.OrderStatus;
import com.mcart.productcatalogsvc.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final UserService userService;

    // ─── Create Order ────────────────────────────────────────────────────────
    // Fix 1: Removed unused bearerToken param — CartService is in same service, direct call
    // Fix 2: Fetch real address from UserService using addressId
    // Fix 3: cartService.getCart() called with correct signature (userId, null)
    // Fix 4: clearCart now returns Mono<Void> (fixed in CartService)
    // Fix 5: Build AddressSnapshot from real UserAddress
    public Mono<OrderResponse> createOrder(
            String userId,
            CreateOrderRequest request
    ) {
        UUID addressId = UUID.fromString(request.getAddressId());
        UUID userUUID  = UUID.fromString(userId);

        // 1. Fetch address and cart in parallel
        Mono<UserAddress> addressMono = userService.getAddresses(userUUID)
                .filter(addr -> addr.getAddressId().equals(addressId))
                .next()
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException("Address not found: " + addressId)
                ));

        Mono<com.mcart.productcatalogsvc.model.CartResponseDto> cartMono =
                cartService.getCart(userId, null);

        return Mono.zip(addressMono, cartMono)
                .flatMap(tuple -> {
                    UserAddress address = tuple.getT1();
                    com.mcart.productcatalogsvc.model.CartResponseDto cart = tuple.getT2();

                    if (cart.getItems() == null || cart.getItems().isEmpty()) {
                        return Mono.error(new IllegalStateException("Cart is empty"));
                    }

                    // 2. Build address snapshot from real address
                    AddressSnapshot addressSnapshot = buildAddressSnapshot(address);

                    // 3. Map cart items → order items
                    List<OrderItem> orderItems = cart.getItems().stream()
                            .map(cartItem -> OrderItem.builder()
                                    .productId(cartItem.getProductId())
                                    .variantId(cartItem.getVariantId())
                                    .quantity(cartItem.getQuantity())
                                    .priceAtOrder(cartItem.getPriceAtAddition())
                                    .build()
                            )
                            .toList();

                    // 4. Calculate totals across ALL items with ALL quantities
                    int totalItems = orderItems.stream()
                            .mapToInt(OrderItem::getQuantity)
                            .sum();

                    double totalAmount = orderItems.stream()
                            .mapToDouble(i -> i.getPriceAtOrder() * i.getQuantity())
                            .sum();

                    String now = Instant.now().toString();

                    // 5. Build order
                    Order order = Order.builder()
                            .orderId("ORD-" + UUID.randomUUID())
                            .userId(userId)
                            .status(OrderStatus.PLACED.name())
                            .paymentMethod(request.getPaymentMethod().toUpperCase())
                            .addressId(request.getAddressId())
                            .addressSnapshot(addressSnapshot)
                            .items(orderItems)
                            .totalItems(totalItems)
                            .totalAmount(totalAmount)
                            .currency(cart.getCurrency() != null ? cart.getCurrency() : "INR")
                            .notes(request.getNotes() != null ? request.getNotes() : "")
                            .createdAt(now)
                            .updatedAt(now)
                            .build();

                    // 6. Save order → clear cart → return response
                    return orderRepository.save(order)
                            .flatMap(saved ->
                                    cartService.clearCart(userId)
                                            .thenReturn(saved)
                            )
                            .map(this::toResponse);
                });
    }

    // ─── Get All Orders for User ─────────────────────────────────────────────
    public Flux<OrderResponse> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId)
                .map(this::toResponse);
    }

    // ─── Get Single Order ────────────────────────────────────────────────────
    public Mono<OrderResponse> getOrderById(String orderId, String userId) {
        return orderRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new OrderNotFoundException(orderId)))
                .flatMap(order -> {
                    if (!order.getUserId().equals(userId)) {
                        return Mono.error(new OrderNotFoundException(orderId));
                    }
                    return Mono.just(toResponse(order));
                });
    }

    // ─── Cancel Order ────────────────────────────────────────────────────────
    public Mono<OrderResponse> cancelOrder(String orderId, String userId) {
        return orderRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new OrderNotFoundException(orderId)))
                .flatMap(order -> {
                    if (!order.getUserId().equals(userId)) {
                        return Mono.error(new OrderNotFoundException(orderId));
                    }
                    if (List.of("SHIPPED", "DELIVERED", "CANCELLED").contains(order.getStatus())) {
                        return Mono.error(new IllegalStateException(
                                "Cannot cancel order in status: " + order.getStatus()
                        ));
                    }
                    order.setStatus(OrderStatus.CANCELLED.name());
                    order.setUpdatedAt(Instant.now().toString());
                    return orderRepository.save(order).map(this::toResponse);
                });
    }

    // ─── Update Status (admin/internal) ──────────────────────────────────────
    public Mono<OrderResponse> updateStatus(String orderId, String status) {
        if (status == null || status.isBlank()) {
            return Mono.error(new IllegalArgumentException("Status cannot be empty"));
        }
        // Validate against enum
        try {
            OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Mono.error(new IllegalArgumentException("Invalid status: " + status));
        }
        return orderRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new OrderNotFoundException(orderId)))
                .flatMap(order -> {
                    order.setStatus(status.toUpperCase());
                    order.setUpdatedAt(Instant.now().toString());
                    return orderRepository.save(order).map(this::toResponse);
                });
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    // Fix 3: Build address snapshot from real UserAddress entity
    private AddressSnapshot buildAddressSnapshot(UserAddress address) {
        Map<String, Object> json = address.getAddressJson();
        if (json == null) {
            return AddressSnapshot.builder()
                    .type(address.getType())
                    .build();
        }
        return AddressSnapshot.builder()
                .type(address.getType())
                .name(getString(json, "name"))
                .phone(getString(json, "phone"))
                .street(getString(json, "street"))
                .area(getString(json, "area"))
                .city(getString(json, "city"))
                .state(getString(json, "state"))
                .pincode(getString(json, "pincode"))
                .build();
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }

    private OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .addressId(order.getAddressId())
                .addressSnapshot(order.getAddressSnapshot())
                .items(order.getItems())
                .totalItems(order.getTotalItems())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}