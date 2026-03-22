// service/WishlistService.java
package com.mcart.productcatalogsvc.service;

import com.mcart.productcatalogsvc.entity.WishlistItem;
import com.mcart.productcatalogsvc.model.WishlistItemDto;
import com.mcart.productcatalogsvc.model.WishlistItemRequestDto;
import com.mcart.productcatalogsvc.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;

    public WishlistService(WishlistRepository wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    // POST /wishlist/add
    public Mono<Void> addToWishlist(String userId, WishlistItemRequestDto request) {
        boolean hasVariant = request.getVariantId() != null;

        // Upsert — update notes/priority if item already exists
        Mono<WishlistItem> existing = hasVariant
                ? wishlistRepository.findByUserIdAndProductIdAndVariantId(
                        userId, request.getProductId(), request.getVariantId())
                : wishlistRepository.findByUserIdAndProductIdAndVariantIdIsNull(
                        userId, request.getProductId());

        return existing
                .flatMap(found -> {
                    // update in place
                    found.setPriority(request.getPriority());
                    found.setNotes(request.getNotes());
                    return wishlistRepository.save(found);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // insert new
                    WishlistItem item = new WishlistItem();
                    item.setUserId(userId);
                    item.setProductId(request.getProductId());
                    item.setVariantId(request.getVariantId());   // null is fine
                    item.setPriority(request.getPriority());
                    item.setNotes(request.getNotes());
                    return wishlistRepository.save(item);
                }))
                .then();
    }

    // DELETE /wishlist/remove
    public Mono<Void> removeFromWishlist(String userId, String productId, String variantId) {
        if (variantId != null) {
            return wishlistRepository
                    .deleteByUserIdAndProductIdAndVariantId(userId, productId, variantId);
        }
        return wishlistRepository
                .deleteByUserIdAndProductIdAndVariantIdIsNull(userId, productId);
    }

    // GET /wishlist
    public Flux<WishlistItemDto> getWishlist(String userId) {
        return wishlistRepository.findByUserId(userId)
                .map(item -> WishlistItemDto.builder()
                        .productId(item.getProductId())
                        .variantId(item.getVariantId())
                        .priority(item.getPriority())
                        .notes(item.getNotes())
                        .addedAt(item.getAddedAt().toString())
                        .build());
    }
}