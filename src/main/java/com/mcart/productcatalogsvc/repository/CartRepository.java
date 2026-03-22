// repository/CartRepository.java  ← same public API as your DynamoDB version
package com.mcart.productcatalogsvc.repository;

import com.mcart.productcatalogsvc.entity.Cart;
import com.mcart.productcatalogsvc.entity.CartItem;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Repository
public class CartRepository {

    private final CartR2dbcRepository cartR2dbcRepository;
    private final CartItemR2dbcRepository cartItemR2dbcRepository;

    public CartRepository(CartR2dbcRepository cartR2dbcRepository,
                          CartItemR2dbcRepository cartItemR2dbcRepository) {
        this.cartR2dbcRepository = cartR2dbcRepository;
        this.cartItemR2dbcRepository = cartItemR2dbcRepository;
    }

    // -----------------------------------------------------------------
    // save(CartItem)
    // Replaces: cartTable.putItem(item)
    // Resolves/creates the parent Cart, then upserts the CartItem row.
    // CartItem must have ownerType + ownerId set by the service layer.
    // -----------------------------------------------------------------
    public Mono<Void> save(CartItem item) {
        return resolveOrCreateCart(item.getOwnerType(), item.getOwnerId())
                .flatMap(cart -> {
                    item.setCartId(cart.getId());
                    return cartItemR2dbcRepository
                            .findByCartIdAndProductIdAndVariantId(
                                    cart.getId(), item.getProductId(), item.getVariantId())
                            .flatMap(existing -> {
                                existing.setQuantity(item.getQuantity());
                                existing.setPriceAtAddition(item.getPriceAtAddition());
                                return cartItemR2dbcRepository.save(existing);
                            })
                            .switchIfEmpty(cartItemR2dbcRepository.save(item));
                })
                .then();
    }

    // -----------------------------------------------------------------
    // deleteItem(pk, sk)
    // Replaces: cartTable.deleteItem(pk, sk)
    // pk = "USER#<userId>" | "GUEST#<token>"
    // sk = "ITEM#<productId>#<variantId>"
    // -----------------------------------------------------------------
    public Mono<Void> deleteItem(String pk, String sk) {
        String[] skParts = sk.split("#", 3);        // ["ITEM", productId, variantId]
        String productId = skParts[1];
        String variantId = skParts[2];

        return resolveCartByPk(pk)
                .flatMap(cart -> cartItemR2dbcRepository
                        .deleteByCartIdAndProductIdAndVariantId(
                                cart.getId(), productId, variantId));
    }

    // -----------------------------------------------------------------
    // findItemsByPk(pk)
    // Replaces: cartTable.query(keyEqualTo(pk))
    // -----------------------------------------------------------------
    public Flux<CartItem> findItemsByPk(String pk) {
        return resolveCartByPk(pk)
                .flatMapMany(cart -> cartItemR2dbcRepository.findByCartId(cart.getId()));
    }

    // -----------------------------------------------------------------
    // deleteAllByPk(pk)
    // Replaces: fetch-all + delete-one-by-one loop in DynamoDB version
    // Now a single DELETE WHERE cart_id = ? — no N+1
    // -----------------------------------------------------------------
    public Mono<Void> deleteAllByPk(String pk) {
        return resolveCartByPk(pk)
                .flatMap(cart -> cartItemR2dbcRepository.deleteByCartId(cart.getId()));
    }

    // -----------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------

    // Finds Cart by raw DynamoDB-style PK string, returns empty if not found
    public Mono<Cart> resolveCartByPk(String pk) {
        if (pk.startsWith("USER#")) {
            return cartR2dbcRepository.findByUserId(pk.substring(5));
        } else if (pk.startsWith("GUEST#")) {
            return cartR2dbcRepository.findByGuestToken(pk.substring(6));
        }
        return Mono.error(new IllegalArgumentException("Unknown PK format: " + pk));
    }

    private Mono<Cart> resolveOrCreateCart(Cart.OwnerType ownerType, String ownerId) {
        if (ownerType == Cart.OwnerType.USER) {
            return cartR2dbcRepository.findByUserId(ownerId)
                    .switchIfEmpty(createCart(ownerType, ownerId));
        } else {
            return cartR2dbcRepository.findByGuestToken(ownerId)
                    .switchIfEmpty(createCart(ownerType, ownerId));
        }
    }

    private Mono<Cart> createCart(Cart.OwnerType ownerType, String ownerId) {
        Cart cart = new Cart();
        cart.setOwnerType(ownerType);
        if (ownerType == Cart.OwnerType.USER) {
            cart.setUserId(ownerId);
        } else {
            cart.setGuestToken(ownerId);
            cart.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
        }
        return cartR2dbcRepository.save(cart);
    }
    
    public Mono<Cart> resolveOrCreateCartByPk(String pk) {
        if (pk.startsWith("USER#")) {
            return resolveOrCreateCart(Cart.OwnerType.USER, pk.substring(5));
        } else if (pk.startsWith("GUEST#")) {
            return resolveOrCreateCart(Cart.OwnerType.GUEST, pk.substring(6));
        }
        return Mono.error(new IllegalArgumentException("Unknown PK format: " + pk));
    }

    // Saves a CartItem that already has cartId set — skips resolveOrCreateCart entirely
    public Mono<Void> saveWithCartId(CartItem item) {
        return cartItemR2dbcRepository
                .findByCartIdAndProductIdAndVariantId(
                        item.getCartId(), item.getProductId(), item.getVariantId())
                .flatMap(existing -> {
                    existing.setQuantity(item.getQuantity());
                    existing.setPriceAtAddition(item.getPriceAtAddition());
                    return cartItemR2dbcRepository.save(existing);
                })
                .switchIfEmpty(cartItemR2dbcRepository.save(item))
                .then();
    }
}