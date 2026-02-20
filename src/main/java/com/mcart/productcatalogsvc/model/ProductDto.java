package com.mcart.productcatalogsvc.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductDto {
    private String productId;
    private String name;
    private String slug;
    private String description;
    private String brandName;
    private Double basePrice;
    private Double salePrice;
    private Integer discountPercentage;
    private Double ratingAverage;
    private Integer ratingCount;
    private String primaryImage;
    private List<String> images;
    private List<String> availableSizes;
    private List<String> availableColors;
    private String material;
    private String fitType;
    private String gender;
    private Boolean isActive;
}