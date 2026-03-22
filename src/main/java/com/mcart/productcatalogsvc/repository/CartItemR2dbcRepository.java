// repository/CartItemR2dbcRepository.java
package com.mcart.productcatalogsvc.repository;

import com.mcart.productcatalogsvc.entity.CartItem;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CartItemR2dbcRepository extends ReactiveCrudRepository<CartItem, Long> {

    Flux<CartItem> findByCartId(Long cartId);

    Mono<CartItem> findByCartIdAndProductIdAndVariantId(
            Long cartId, String productId, String variantId);

    Mono<Void> deleteByCartId(Long cartId);

    Mono<Void> deleteByCartIdAndProductIdAndVariantId(
            Long cartId, String productId, String variantId);
}