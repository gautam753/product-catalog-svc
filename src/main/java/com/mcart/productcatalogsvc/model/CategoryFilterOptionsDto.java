package com.mcart.productcatalogsvc.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class CategoryFilterOptionsDto {

    private String categoryId;
    private String categoryName;
    private String categorySlug;

    // Aggregated filter values
    private List<FilterOption> brands;
    private PriceRange priceRange;
    private List<FilterOption> sizes;
    private List<FilterOption> colors;
    private List<FilterOption> materials;
    private List<FilterOption> fitTypes;
    private List<FilterOption> genders;

    // Optional: count of products in this category
    private Long productCount;

    @Data
    @Builder
    public static class FilterOption {
    	private String key;
        private String value;     // e.g. "Levi's", "Blue", "M"
        private String label;     // e.g. "Levi's (45)", "Blue (32)"
        private Long count;       // number of products matching this value
    }

    @Data
    @Builder
    public static class PriceRange {
        private Double minPrice;
        private Double maxPrice;
        private List<PriceBucket> buckets; // optional: predefined ranges like 0-500, 500-1000...
    }

    @Data
    @Builder
    public static class PriceBucket {
        private String range;     // e.g. "0-500", "500-1000"
        private Long count;
    }
}