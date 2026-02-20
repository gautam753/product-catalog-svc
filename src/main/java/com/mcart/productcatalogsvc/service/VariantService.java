package com.mcart.productcatalogsvc.service;

import com.mcart.productcatalogsvc.model.VariantDto;
import com.mcart.productcatalogsvc.entity.Variant;
import com.mcart.productcatalogsvc.repository.VariantRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class VariantService {

    private final VariantRepository variantRepository;

    public VariantService(VariantRepository variantRepository) {
        this.variantRepository = variantRepository;
    }

    public Flux<VariantDto> getVariantsByProductId(String productId) {
        return variantRepository.findByProductId(productId)
            .map(this::toDto);
    }

    public Mono<VariantDto> getVariantById(String productId, String variantId) {
        return variantRepository.findById(productId, variantId)
            .map(this::toDto);
    }

    public Mono<VariantDto> getVariantBySku(String sku) {
        return variantRepository.findBySku(sku)
            .map(this::toDto);
    }

    public Flux<VariantDto> getLowStockVariants(int threshold) {
        return variantRepository.findLowStock(threshold)
            .map(this::toDto);
    }

    private VariantDto toDto(Variant variant) {
        return VariantDto.builder()
            .variantId(variant.getVariantId())
            .productId(variant.getProductId())
            .color(variant.getColor())
            .size(variant.getSize())
            .sku(variant.getSku())
            .price(variant.getPrice())
            .mrp(variant.getMrp())
            .stockQuantity(variant.getStockQuantity())
            .availableStock(variant.getAvailableStock())
            .barcode(variant.getBarcode())
            .weight(variant.getWeight())
            .build();
    }
}
