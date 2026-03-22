// repository/CartR2dbcRepository.java
package com.mcart.productcatalogsvc.repository;

import com.mcart.productcatalogsvc.entity.Cart;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
public interface CartR2dbcRepository extends ReactiveCrudRepository<Cart, Long> {

    Mono<Cart> findByUserId(String userId);

    Mono<Cart> findByGuestToken(String guestToken);

    @Query("DELETE FROM carts WHERE expires_at < :now AND owner_type = 'GUEST'")
    Mono<Void> deleteExpiredGuestCarts(Instant now);
}