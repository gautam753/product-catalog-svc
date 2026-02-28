package com.mcart.productcatalogsvc.controller;

import java.util.Map;

import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mcart.productcatalogsvc.model.CartItemRequestDto;
import com.mcart.productcatalogsvc.model.CartResponseDto;
import com.mcart.productcatalogsvc.service.CartService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }
    
    @GetMapping("/debug")
    public Mono<Map<String, String>> headers(ServerHttpRequest request) {
        return Mono.just(
            request.getHeaders().toSingleValueMap()
        );
    }

    // POST /api/cart/add
    @PostMapping("/add")
    public Mono<CartResponseDto> addToCart(
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @RequestBody CartItemRequestDto request,
            @RequestHeader(value = "authoeized-user-id", required = false) String authorizedUserId
            //@AuthenticationPrincipal Jwt jwt
            ) {  // Spring Security JWT

        String userId = (authorizedUserId/*jwt*/ != null) ? authorizedUserId/*jwt.getSubject()*/ : null;
        return cartService.addToCart(userId, guestToken, request);
    }

    // GET /api/cart
    @GetMapping
    public Mono<CartResponseDto> getCart(
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @RequestHeader(value = "authoeized-user-id", required = false) String authorizedUserId
            //@AuthenticationPrincipal Jwt jwt
            ) {
    	log.info("authoeized-user-id: {}, X-Guest-Token: {}", authorizedUserId, guestToken);
        String userId = (authorizedUserId/*jwt*/ != null) ? authorizedUserId/*jwt.getSubject()*/ : null;
        return cartService.getCart(userId, guestToken);
    }

    // DELETE /api/cart/remove
    @DeleteMapping("/remove")
    public Mono<Void> removeFromCart(
            @RequestParam String productId,
            @RequestParam(required = false) String variantId,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @RequestHeader(value = "authoeized-user-id", required = false) String authorizedUserId
            //@AuthenticationPrincipal Jwt jwt
            ) {

        String userId = (authorizedUserId/*jwt*/ != null) ? authorizedUserId/*jwt.getSubject()*/ : null;
        return cartService.removeFromCart(userId, guestToken, productId, variantId);
    }

    // POST /api/cart/merge (called by frontend after login)
    @PostMapping("/merge")
    public Mono<CartResponseDto> mergeGuestCart(
            @RequestHeader("X-Guest-Token") String guestToken,
            @RequestHeader(value = "authoeized-user-id", required = false) String authorizedUserId
            //@AuthenticationPrincipal Jwt jwt
            ) {

        String userId = authorizedUserId/*jwt.getSubject()*/;
        return cartService.mergeGuestCart(userId, guestToken);
    }
}
