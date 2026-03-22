// service/CartService.java
package com.mcart.productcatalogsvc.service;

import com.mcart.productcatalogsvc.entity.Cart;
import com.mcart.productcatalogsvc.entity.CartItem;
import com.mcart.productcatalogsvc.exception.NotFoundException;
import com.mcart.productcatalogsvc.model.CartItemDto;
import com.mcart.productcatalogsvc.model.CartItemRequestDto;
import com.mcart.productcatalogsvc.model.CartResponseDto;
import com.mcart.productcatalogsvc.orders.dto.OrderResponse;
import com.mcart.productcatalogsvc.repository.CartRepository;
import com.mcart.productcatalogsvc.repository.ProductRepository;
import com.mcart.productcatalogsvc.repository.VariantRepository;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final VariantRepository variantRepository;

    public CartService(CartRepository cartRepository,
                       ProductRepository productRepository,
                       VariantRepository variantRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    // POST /cart/add
    public Mono<CartResponseDto> addToCart(String userId, String guestToken,
                                           CartItemRequestDto request) {
        if (userId == null && guestToken == null) {
            return Mono.error(new IllegalArgumentException("userId or guestToken required"));
        }

        Cart.OwnerType ownerType = (userId != null) ? Cart.OwnerType.USER : Cart.OwnerType.GUEST;
        String ownerId  = (userId != null) ? userId : guestToken;
        // kept for getCart() + removeFromCart() compatibility
        String pk       = (userId != null) ? "USER#" + userId : "GUEST#" + guestToken;

        String variantPart = request.getVariantId() != null ? request.getVariantId() : "DEFAULT";

        return productRepository.findById(request.getProductId())
                .switchIfEmpty(Mono.error(new NotFoundException("Product not found")))
                .flatMap(product -> variantRepository.findById(product.getProductId(), variantPart)
                        .switchIfEmpty(Mono.error(new NotFoundException("Variant not found")))
                        .flatMap(variant -> {
                            CartItem item = new CartItem();
                            item.setProductId(request.getProductId());
                            item.setVariantId(variant.getVariantId());
                            item.setQuantity(request.getQuantity());
                            item.setPriceAtAddition(BigDecimal.valueOf(variant.getPrice()));
                            // transient fields — used by CartRepository to resolve parent Cart
                            item.setOwnerType(ownerType);
                            item.setOwnerId(ownerId);

                            return cartRepository.save(item)
                                    .then(getCart(pk));
                        }));
    }

    // GET /cart
    public Mono<CartResponseDto> getCart(String userId, String guestToken) {
        if (userId == null && guestToken == null) {
            return Mono.just(CartResponseDto.builder().items(List.of()).build());
        }
        String pk = (userId != null) ? "USER#" + userId : "GUEST#" + guestToken;
        return getCart(pk);
    }

    // DELETE /cart/remove
    public Mono<Void> removeFromCart(String userId, String guestToken,
                                     String productId, String variantId) {
        String pk = (userId != null) ? "USER#" + userId : "GUEST#" + guestToken;
        String sk = "ITEM#" + productId + "#" + (variantId != null ? variantId : "DEFAULT");
        return cartRepository.deleteItem(pk, sk);
    }

    // POST /cart/merge  — called on login/signup
    public Mono<CartResponseDto> mergeGuestCart(String userId, String guestToken) {
        if (guestToken == null) {
            return getCart("USER#" + userId);
        }

        String guestPk = "GUEST#" + guestToken;
        String userPk  = "USER#" + userId;

        // Resolve/create the user cart ONCE upfront, then assign all items to it.
        // Previously flatMap let each item race to create the cart → duplicate rows.
        return cartRepository.resolveOrCreateCartByPk(userPk)
                .flatMap(userCart ->
                    cartRepository.findItemsByPk(guestPk)
                            .flatMap(item -> {
                                item.setCartId(userCart.getId());   // assign directly — no re-resolution
                                item.setOwnerType(Cart.OwnerType.USER);
                                item.setOwnerId(userId);
                                return cartRepository.saveWithCartId(item);  // skip resolveOrCreateCart
                            })
                            .thenMany(cartRepository.deleteAllByPk(guestPk))
                            .then(getCart(userPk))
                );
    }

    // ------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------
    private Mono<CartResponseDto> getCart(String pk) {
    	Flux<CartItem> cartItem = cartRepository.findItemsByPk(pk);
        return cartItem
                .collectList()
                .map(items -> {
                    int totalItems = items.stream()
                            .mapToInt(CartItem::getQuantity)
                            .sum();
                    double totalPrice = items.stream()
                            .mapToDouble(i -> i.getQuantity()
                                    * i.getPriceAtAddition().doubleValue())
                            .sum();

                    List<CartItemDto> dtos = items.stream()
                            .map(i -> CartItemDto.builder()
                                    .productId(i.getProductId())
                                    .variantId(i.getVariantId())
                                    .quantity(i.getQuantity())
                                    .priceAtAddition(i.getPriceAtAddition().doubleValue())
                                    .addedAt(i.getAddedAt().toString())
                                    .build())
                            .collect(Collectors.toList());

                    return CartResponseDto.builder()
                            .cartId(pk.replaceFirst("^(USER|GUEST)#", ""))
                            .items(dtos)
                            .totalItems(totalItems)
                            .totalPrice(totalPrice)
                            .currency("INR")
                            .build();
                });
    }

    public Mono<Void> clearCart(String userId) {
        if (userId == null) return Mono.empty();
        String pk = "USER#" + userId;
        return cartRepository.deleteAllByPk(pk);
    }
}