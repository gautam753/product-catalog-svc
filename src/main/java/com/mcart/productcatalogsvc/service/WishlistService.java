package com.mcart.productcatalogsvc.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.mcart.productcatalogsvc.entity.WishlistItem;
import com.mcart.productcatalogsvc.model.WishlistItemDto;
import com.mcart.productcatalogsvc.model.WishlistItemRequestDto;
import com.mcart.productcatalogsvc.repository.WishlistRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;

    public WishlistService(WishlistRepository wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    // Add to wishlist (authenticated only)
    public Mono<Void> addToWishlist(String userId, WishlistItemRequestDto request) {
        String pk = "USER#" + userId;
        String sk = "ITEM#" + request.getProductId() +
            (request.getVariantId() != null ? "#" + request.getVariantId() : "");

        WishlistItem item = new WishlistItem();
        item.setPK(pk);
        item.setSK(sk);
        item.setProductId(request.getProductId());
        item.setVariantId(request.getVariantId());
        item.setPriority(request.getPriority());
        item.setNotes(request.getNotes());
        item.setAddedAt(Instant.now().toString());

        return wishlistRepository.save(item);
    }

    // Remove from wishlist
    public Mono<Void> removeFromWishlist(String userId, String productId, String variantId) {
        String pk = "USER#" + userId;
        String sk = "ITEM#" + productId +
            (variantId != null ? "#" + variantId : "");
        return wishlistRepository.deleteItem(pk, sk);
    }

    // Get wishlist
    public Flux<WishlistItemDto> getWishlist(String userId) {
        String pk = "USER#" + userId;
        return wishlistRepository.findByPk(pk)
            .map(item -> WishlistItemDto.builder()
                .productId(item.getProductId())
                .variantId(item.getVariantId())
                .priority(item.getPriority())
                .notes(item.getNotes())
                .addedAt(item.getAddedAt())
                .build());
    }
}
