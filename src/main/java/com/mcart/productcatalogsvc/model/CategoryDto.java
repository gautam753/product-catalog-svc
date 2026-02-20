package com.mcart.productcatalogsvc.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategoryDto {
    private String categoryId;
    private String name;
    private String slug;
    private String parentCategoryId;
    private Integer level;
    private Boolean isLeaf;
    private Integer displayOrder;
    private String description;
    private String image;
    private List<CategoryDto> subcategories; // for tree response
}
