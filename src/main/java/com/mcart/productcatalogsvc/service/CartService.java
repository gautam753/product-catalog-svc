package com.mcart.productcatalogsvc.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mcart.productcatalogsvc.entity.CartItem;
import com.mcart.productcatalogsvc.exception.NotFoundException;
import com.mcart.productcatalogsvc.model.CartItemDto;
import com.mcart.productcatalogsvc.model.CartItemRequestDto;
import com.mcart.productcatalogsvc.model.CartResponseDto;
import com.mcart.productcatalogsvc.repository.CartRepository;
import com.mcart.productcatalogsvc.repository.ProductRepository;
import com.mcart.productcatalogsvc.repository.VariantRepository;

import reactor.core.publisher.Mono;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final VariantRepository variantRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository,
                       VariantRepository variantRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    // POST /api/cart/add
    public Mono<CartResponseDto> addToCart(String userId, String guestToken,
                                           CartItemRequestDto request) {
        String pkPrefix = (userId != null) ? "USER#" + userId : "GUEST#" + guestToken;
        if (pkPrefix.startsWith("GUEST#") && guestToken == null) {
            return Mono.error(new IllegalArgumentException("guestToken required for guest users"));
        }

        String cartPk = pkPrefix;
        String variantPart = request.getVariantId() != null ? request.getVariantId() : "DEFAULT";
        String itemSk = "ITEM#" + request.getProductId() + "#" + variantPart;
        // Validate product & variant exist (optional)
        return productRepository.findById(request.getProductId())
            .switchIfEmpty(Mono.error(new NotFoundException("Product not found")))
            .flatMap(product -> variantRepository.findById(product.getProductId(), variantPart)
                .switchIfEmpty(Mono.error(new NotFoundException("Variant not found")))
                .flatMap(variant -> {
                    CartItem item = new CartItem();
                    item.setPK(cartPk);
                    item.setSK(itemSk);
                    item.setProductId(request.getProductId());
                    item.setVariantId(variant.getVariantId());
                    item.setQuantity(request.getQuantity());
                    item.setPriceAtAddition(variant.getPrice());
                    item.setAddedAt(Instant.now().toString());
                    item.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS).getEpochSecond());

                    return cartRepository.save(item)
                        .then(getCart(cartPk));
                }));
    }

    // GET /api/cart
    public Mono<CartResponseDto> getCart(String userId, String guestToken) {
        String pkPrefix = (userId != null) ? "USER#" + userId : "GUEST#" + guestToken;
        if (pkPrefix.startsWith("GUEST#") && guestToken == null) {
            return Mono.just(CartResponseDto.builder().items(List.of()).build());
        }
        return getCart(pkPrefix);
    }

    private Mono<CartResponseDto> getCart(String pk) {
        return cartRepository.findItemsByPk(pk)
            .collectList()
            .map(items -> {
                int totalItems = items.stream()
                    .mapToInt(CartItem::getQuantity)
                    .sum();
                double totalPrice = items.stream()
                    .mapToDouble(i -> i.getQuantity() * i.getPriceAtAddition())
                    .sum();

                List<CartItemDto> dtos = items.stream()
                    .map(i -> CartItemDto.builder()
                        .productId(i.getProductId())
                        .variantId(i.getVariantId())
                        .quantity(i.getQuantity())
                        .priceAtAddition(i.getPriceAtAddition())
                        .addedAt(i.getAddedAt())
                        .build())
                    .collect(Collectors.toList());

                return CartResponseDto.builder()
                    .cartId(pk.replace("USER#", "").replace("GUEST#", ""))
                    .items(dtos)
                    .totalItems(totalItems)
                    .totalPrice(totalPrice)
                    .currency("INR")
                    .build();
            });
    }

    // Remove from cart
    public Mono<Void> removeFromCart(String userId, String guestToken,
                                     String productId, String variantId) {
        String pkPrefix = (userId != null) ? "USER#" + userId : "GUEST#" + guestToken;
        String itemSk = "ITEM#" + productId + "#" + variantId;
        return cartRepository.deleteItem(pkPrefix, itemSk);
    }

    // POST /api/cart/merge (called on login/signup)
    public Mono<CartResponseDto> mergeGuestCart(String userId, String guestToken) {
        if (guestToken == null) {
            return getCart("USER#" + userId);
        }

        String guestPk = "GUEST#" + guestToken;
        String userPk = "USER#" + userId;

        return cartRepository.findItemsByPk(guestPk)
            .flatMap(item -> {
                // Change PK to user
                item.setPK(userPk);
                return cartRepository.save(item);
            })
            .thenMany(cartRepository.deleteAllByPk(guestPk))
            .then(getCart(userPk));
    }
}
