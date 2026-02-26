package com.mcart.productcatalogsvc.controller;

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

import com.mcart.productcatalogsvc.model.WishlistItemDto;
import com.mcart.productcatalogsvc.model.WishlistItemRequestDto;
import com.mcart.productcatalogsvc.service.WishlistService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping("/add")
    public Mono<Void> addToWishlist(
            @RequestBody WishlistItemRequestDto request,
            @RequestHeader(value = "authoeized-user-id", required = false) String authorizedUserId
            //@AuthenticationPrincipal Jwt jwt
            ) {

        String userId = authorizedUserId/*jwt.getSubject()*/;
        return wishlistService.addToWishlist(userId, request);
    }

    @DeleteMapping("/remove")
    public Mono<Void> removeFromWishlist(
            @RequestParam String productId,
            @RequestParam(required = false) String variantId,
            @RequestHeader(value = "authoeized-user-id", required = false) String authorizedUserId
            //@AuthenticationPrincipal Jwt jwt
            ) {

        String userId = authorizedUserId/*jwt.getSubject()*/;
        return wishlistService.removeFromWishlist(userId, productId, variantId);
    }

    @GetMapping
    public Flux<WishlistItemDto> getWishlist(@RequestHeader(value = "authoeized-user-id", required = false) String authorizedUserId
            //@AuthenticationPrincipal Jwt jwt
            ) {
        String userId = authorizedUserId/*jwt.getSubject()*/;
        return wishlistService.getWishlist(userId);
    }
}
