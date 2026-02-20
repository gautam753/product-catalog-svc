package com.mcart.productcatalogsvc.controller;

import com.mcart.productcatalogsvc.model.ProductDto;
import com.mcart.productcatalogsvc.service.ProductService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ProductDto> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{productId}")
    public Mono<ProductDto> getProductById(@PathVariable String productId) {
        return productService.getProductById(productId);
    }

    @GetMapping("/slug/{slug}")
    public Mono<ProductDto> getProductBySlug(@PathVariable String slug) {
        return productService.getProductBySlug(slug);
    }

    @GetMapping("/search")
    public Flux<ProductDto> searchProducts(@RequestParam String q) {
        return productService.searchProducts(q);
    }

    @GetMapping("/brand/{brandId}")
    public Flux<ProductDto> getProductsByBrand(@PathVariable String brandId) {
        return productService.getProductsByBrand(brandId);
    }

    // Optional: Add more filters (category, price range, etc.) later
}

/*
Endpoint,Method,Purpose,Typical Frontend Use Case
/api/products,GET,List all products (paginated in future),"Homepage featured products, explore page"
/api/products/{productId},GET,Get full product details,Product detail page
/api/products/slug/{slug},GET,Get product by SEO-friendly slug,SEO URLs (/products/levis-511-jeans)
/api/products/search?q=...,GET,Simple name-based search,Search bar
/api/products/brand/{brandId},GET,Products by brand,Brand page (/brands/levis)
*/