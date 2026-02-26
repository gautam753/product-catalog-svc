package com.mcart.productcatalogsvc.model;

import lombok.Data;

@Data
public class CartItemRequestDto {
    private String productId;
    private String variantId;
    private Integer quantity;
}
