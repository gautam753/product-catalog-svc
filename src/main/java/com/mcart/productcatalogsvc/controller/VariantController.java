package com.mcart.productcatalogsvc.controller;

import com.mcart.productcatalogsvc.model.VariantDto;
import com.mcart.productcatalogsvc.service.VariantService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/variants")
public class VariantController {

    private final VariantService variantService;

    public VariantController(VariantService variantService) {
        this.variantService = variantService;
    }

    /**
     * Get all variants for a specific product
     * Used in product detail page to show size/color options
     */
    @GetMapping("/product/{productId}")
    public Flux<VariantDto> getVariantsByProduct(@PathVariable String productId) {
        return variantService.getVariantsByProductId(productId);
    }

    /**
     * Get single variant by productId + variantId
     */
    @GetMapping("/{productId}/{variantId}")
    public Mono<VariantDto> getVariantById(
            @PathVariable String productId,
            @PathVariable String variantId) {
        return variantService.getVariantById(productId, variantId);
    }

    /**
     * Get variant by SKU (useful for quick lookup from cart/order)
     */
    @GetMapping("/sku/{sku}")
    public Mono<VariantDto> getVariantBySku(@PathVariable String sku) {
        return variantService.getVariantBySku(sku);
    }

    /**
     * Admin endpoint: Variants with low stock
     */
    @GetMapping("/low-stock")
    public Flux<VariantDto> getLowStockVariants(@RequestParam(defaultValue = "10") int threshold) {
        return variantService.getLowStockVariants(threshold);
    }
}
/*
Endpoint,Method,Purpose,Used In Frontend For
/api/variants/product/{productId},GET,List all variants of a product,Product detail page (size/color picker)
/api/variants/{productId}/{variantId},GET,Get specific variant details,Cart / checkout (selected variant info)
/api/variants/sku/{sku},GET,Lookup variant by SKU,Order processing / inventory check
/api/variants/low-stock,GET,Admin: low stock variants,Admin dashboard
*/