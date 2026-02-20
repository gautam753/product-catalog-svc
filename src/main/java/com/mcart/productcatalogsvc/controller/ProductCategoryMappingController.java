package com.mcart.productcatalogsvc.controller;

import com.mcart.productcatalogsvc.service.ProductCategoryMappingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/product-category-mapping")
public class ProductCategoryMappingController {

    private final ProductCategoryMappingService mappingService;

    public ProductCategoryMappingController(ProductCategoryMappingService mappingService) {
        this.mappingService = mappingService;
    }

    /**
     * Get all product IDs in a category
     * Frontend: category page → fetch product list
     */
    @GetMapping("/category/{categoryId}/products")
    public Flux<String> getProductsInCategory(@PathVariable String categoryId) {
        return mappingService.getProductIdsByCategory(categoryId);
    }

    /**
     * Get all category IDs for a product
     * Frontend: product detail → show "Categories" or breadcrumbs
     */
    @GetMapping("/product/{productId}/categories")
    public Flux<String> getCategoriesForProduct(@PathVariable String productId) {
        return mappingService.getCategoryIdsByProduct(productId);
    }

    /**
     * Check if product belongs to category
     */
    @GetMapping("/check")
    public Mono<Boolean> isProductInCategory(
            @RequestParam String productId,
            @RequestParam String categoryId) {
        return mappingService.isProductInCategory(productId, categoryId);
    }

    /**
     * Assign product to category (admin endpoint)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> addMapping(
            @RequestParam String categoryId,
            @RequestParam String productId) {
        return mappingService.addMapping(categoryId, productId);
    }

    /**
     * Remove product from category (admin endpoint)
     */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> removeMapping(
            @RequestParam String categoryId,
            @RequestParam String productId) {
        return mappingService.removeMapping(categoryId, productId);
    }
}
/*
Endpoint,Method,Purpose,Typical Frontend Use Case
/api/product-category-mapping/category/{categoryId}/products,GET,List product IDs in category,Category page → load product cards
/api/product-category-mapping/product/{productId}/categories,GET,List category IDs for product,Product detail → show breadcrumbs or tags
/api/product-category-mapping/check,GET,Check if product ∈ category,Validation before add-to-cart / wishlist
/api/product-category-mapping (POST),POST,Assign product to category (admin),Admin panel → edit product categories
/api/product-category-mapping (DELETE),DELETE,Remove product from category (admin),Admin panel → remove category assignment
*/ 
