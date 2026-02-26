package com.mcart.productcatalogsvc.model;

import lombok.Data;

@Data
public class WishlistItemRequestDto {
    private String productId;
    private String variantId; // optional
    private String priority;  // low/medium/high
    private String notes;
}
