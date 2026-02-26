package com.mcart.productcatalogsvc.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductFilterDto {
    private String categoryId;          // exact category
    private String categorySlug;        // alternative: search by slug
    private String brandId;
    private String brandName;
    private Double minPrice;
    private Double maxPrice;
    private List<String> sizes;         // e.g. ["M", "L"]
    private List<String> colors;        // e.g. ["Blue", "Black"]
    private String gender;              // "men", "women", "unisex"
    private String material;            // e.g. "Cotton"
    private String fitType;             // "Slim", "Regular"
    private Boolean isActive;           // default true
    private Boolean onSale;             // only discounted products
    private String sortBy;              // "price_asc", "price_desc", "rating_desc", "newest"
    private Integer page;               // 0-based
    private Integer size;               // page size
}
