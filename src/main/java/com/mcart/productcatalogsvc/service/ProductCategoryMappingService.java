package com.mcart.productcatalogsvc.service;

import com.mcart.productcatalogsvc.model.ProductCategoryMappingDto;
import com.mcart.productcatalogsvc.entity.ProductCategoryMapping;
import com.mcart.productcatalogsvc.repository.ProductCategoryMappingRepository;

import java.util.Objects;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductCategoryMappingService {

    private final ProductCategoryMappingRepository mappingRepository;

    public ProductCategoryMappingService(ProductCategoryMappingRepository mappingRepository) {
        this.mappingRepository = mappingRepository;
    }

    /**
     * Get all product IDs mapped to a category
     * (Frontend: category page → list products)
     */
    public Flux<String> getProductIdsByCategory(String categoryId) {
        return mappingRepository.findByCategoryId(categoryId)
            .map(ProductCategoryMapping::getProductId);
    }

    /**
     * Get all category IDs a product belongs to
     * (Frontend: product detail → show in breadcrumbs or "also in" sections)
     */
    public Flux<String> getCategoryIdsByProduct(String productId) {
        return mappingRepository.findByProductId(productId)
            .map(ProductCategoryMapping::getCategoryId);
    }

    /**
     * Check if product is mapped to a specific category
     */
    public Mono<Boolean> isProductInCategory(String productId, String categoryId) {
        return mappingRepository.findByCompositeKey(categoryId, productId)
            .map(Objects::nonNull)
            .defaultIfEmpty(false);
    }

    /**
     * Add mapping (assign product to category)
     */
    public Mono<Void> addMapping(String categoryId, String productId) {
        ProductCategoryMapping mapping = new ProductCategoryMapping();
        mapping.setCategoryId(categoryId);
        mapping.setProductId(productId);
        mapping.setCreatedAt(java.time.Instant.now().toString());
        mapping.setUpdatedAt(java.time.Instant.now().toString());

        return mappingRepository.save(mapping);
    }

    /**
     * Remove mapping (unassign product from category)
     */
    public Mono<Void> removeMapping(String categoryId, String productId) {
        return mappingRepository.delete(categoryId, productId);
    }

    // Optional: DTO conversion if needed in controller
    private ProductCategoryMappingDto toDto(ProductCategoryMapping mapping) {
        return ProductCategoryMappingDto.builder()
            .categoryId(mapping.getCategoryId())
            .productId(mapping.getProductId())
            .createdAt(mapping.getCreatedAt())
            .updatedAt(mapping.getUpdatedAt())
            .build();
    }
}
