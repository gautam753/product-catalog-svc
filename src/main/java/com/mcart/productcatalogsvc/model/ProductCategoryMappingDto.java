package com.mcart.productcatalogsvc.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductCategoryMappingDto {
    private String categoryId;
    private String productId;
    private String createdAt;
    private String updatedAt;
}
