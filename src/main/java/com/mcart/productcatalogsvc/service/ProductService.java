package com.mcart.productcatalogsvc.service;

import com.mcart.productcatalogsvc.model.ProductDto;
import com.mcart.productcatalogsvc.entity.Product;
import com.mcart.productcatalogsvc.repository.ProductRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Flux<ProductDto> getAllProducts() {
        return productRepository.findAll()
            .map(this::toDto);
    }

    public Mono<ProductDto> getProductById(String productId) {
        return productRepository.findById(productId)
            .map(this::toDto);
    }

    public Mono<ProductDto> getProductBySlug(String slug) {
        return productRepository.findBySlug(slug)
            .map(this::toDto);
    }

    public Flux<ProductDto> searchProducts(String query) {
        return productRepository.searchByNameContaining(query)
            .map(this::toDto);
    }

    public Flux<ProductDto> getProductsByBrand(String brandId) {
        return productRepository.findByBrand(brandId)
            .map(this::toDto);
    }

    private ProductDto toDto(Product product) {
        return ProductDto.builder()
            .productId(product.getProductId())
            .name(product.getName())
            .slug(product.getSlug())
            .description(product.getDescription())
            .brandName(product.getBrandName())
            .basePrice(product.getBasePrice())
            .salePrice(product.getSalePrice())
            .discountPercentage(product.getDiscountPercentage())
            .ratingAverage(product.getRatingAverage())
            .ratingCount(product.getRatingCount())
            .primaryImage(product.getPrimaryImage())
            .images(product.getImages())
            .availableSizes(product.getAvailableSizes())
            .availableColors(product.getAvailableColors())
            .material(product.getMaterial())
            .fitType(product.getFitType())
            .gender(product.getGender())
            .isActive(product.getIsActive())
            .build();
    }
}
