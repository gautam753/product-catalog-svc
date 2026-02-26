package com.mcart.productcatalogsvc.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemDto {
    private String productId;
    private String variantId;
    private Integer quantity;
    private Double priceAtAddition;
    private String addedAt;
}
