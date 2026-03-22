// repository/WishlistRepository.java
package com.mcart.productcatalogsvc.repository;

import com.mcart.productcatalogsvc.entity.WishlistItem;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface WishlistRepository extends ReactiveCrudRepository<WishlistItem, Long> {

    // Replaces: findByPk("USER#<userId>")
    Flux<WishlistItem> findByUserId(String userId);

    // Replaces: deleteItem(pk, sk) where sk = "ITEM#<productId>#<variantId>"
    Mono<Void> deleteByUserIdAndProductIdAndVariantId(
            String userId, String productId, String variantId);

    // Replaces: deleteItem(pk, sk) where sk = "ITEM#<productId>" (no variant)
    @Query("DELETE FROM wishlist_items WHERE user_id = :userId AND product_id = :productId AND variant_id IS NULL")
    Mono<Void> deleteByUserIdAndProductIdAndVariantIdIsNull(
            String userId, String productId);

    // Replaces: findByPk + filter for duplicate check before save
    Mono<WishlistItem> findByUserIdAndProductIdAndVariantId(
            String userId, String productId, String variantId);

    @Query("SELECT * FROM wishlist_items WHERE user_id = :userId AND product_id = :productId AND variant_id IS NULL")
    Mono<WishlistItem> findByUserIdAndProductIdAndVariantIdIsNull(
            String userId, String productId);
}