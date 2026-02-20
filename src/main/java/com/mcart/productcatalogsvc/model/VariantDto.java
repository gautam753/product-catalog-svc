package com.mcart.productcatalogsvc.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VariantDto {
    private String variantId;
    private String productId;
    private String color;
    private String size;
    private String sku;
    private Double price;
    private Double mrp;
    private Integer stockQuantity;
    private Integer availableStock;
    private String barcode;
    private String weight;
}
