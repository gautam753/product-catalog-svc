package com.mcart.productcatalogsvc.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WishlistItemDto {
    private String productId;
    private String variantId;
    private String priority;
    private String notes;
    private String addedAt;
}
