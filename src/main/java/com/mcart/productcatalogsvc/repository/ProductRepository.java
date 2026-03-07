package com.mcart.productcatalogsvc.repository;

import com.mcart.productcatalogsvc.entity.Product;
import com.mcart.productcatalogsvc.entity.ProductCategoryMapping;
import com.mcart.productcatalogsvc.model.CategoryFilterOptionsDto;
import com.mcart.productcatalogsvc.model.ProductFilterDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Repository
public class ProductRepository {

    private final DynamoDbAsyncTable<Product> productTable;
    private final ProductCategoryMappingRepository mappingRepository;

    public ProductRepository(DynamoDbEnhancedAsyncClient enhancedClient, ProductCategoryMappingRepository mappingRepository) {
        this.productTable = enhancedClient.table(
                "mcart-Products",
                TableSchema.fromBean(Product.class)
        );
		this.mappingRepository = mappingRepository;
    }

    // ================================
    // Find by Primary Key
    // ================================
    public Mono<Product> findById(String productId) {

        Key key = Key.builder()
                .partitionValue(productId)
                .build();

        return Mono.fromFuture(
                productTable.getItem(
                        GetItemEnhancedRequest.builder()
                                .key(key)
                                .build()
                )
        );
    }

    // ================================
    // Find by Slug (GSI)
    // ================================
    public Mono<Product> findBySlug(String slug) {

        return Flux.from(
                productTable.index("GSI_ProductBySlug")
                        .query(QueryEnhancedRequest.builder()
                                .queryConditional(
                                        QueryConditional.keyEqualTo(
                                                Key.builder()
                                                        .partitionValue(slug)
                                                        .build()
                                        )
                                )
                                .limit(1)
                                .build()
                        )
        ).flatMap(page -> Flux.fromIterable(page.items()))
        .next();
    }

    // ================================
    // Scan All
    // ================================
    public Flux<Product> findAll() {
        return Flux.from(productTable.scan().items());
    }

    // ================================
    // Find by Brand (GSI)
    // ================================
    public Flux<Product> findByBrand(String brandId) {

        return Flux.from(
                productTable.index("GSI_ProductByBrand")
                        .query(QueryEnhancedRequest.builder()
                                .queryConditional(
                                        QueryConditional.keyEqualTo(
                                                Key.builder()
                                                        .partitionValue(brandId)
                                                        .build()
                                        )
                                )
                                .build()
                        )
        ).flatMap(page -> Flux.fromIterable(page.items()));
    }

    // ================================
    // Simple Name Search (NOT for prod)
    // ================================
    public Flux<Product> searchByNameContaining(String query) {
        return findAll()
                .filter(p ->
                        p.getName() != null &&
                                p.getName().toLowerCase().contains(query.toLowerCase())
                );
    }

    // ================================
    // Save
    // ================================
    public Mono<Void> save(Product product) {
        return Mono.fromFuture(productTable.putItem(product)).then();
    }

    // ================================
    // Delete
    // ================================
    public Mono<Void> deleteById(String productId) {

        Key key = Key.builder()
                .partitionValue(productId)
                .build();

        return Mono.fromFuture(
                productTable.deleteItem(
                        DeleteItemEnhancedRequest.builder()
                                .key(key)
                                .build()
                )
        ).then();
    }
    
    public Flux<Product> filterProducts(ProductFilterDto filter) {
        // Default pagination
        int page = Math.max(0, filter.getPage() != null ? filter.getPage() : 0);
        int size = Math.max(1, filter.getSize() != null ? filter.getSize() : 20);

        // 1. If categoryId is present → start from ProductCategoryMapping (efficient)
        if (filter.getCategoryId() != null) {
            return filterByCategoryWithAdditionalFilters(filter);
        }

        // 2. No category → full scan + dynamic filter expression
        ScanEnhancedRequest.Builder scanBuilder = ScanEnhancedRequest.builder();

        Expression.Builder exprBuilder = Expression.builder();
        List<String> conditions = new ArrayList<>();
        Map<String, AttributeValue> values = new HashMap<>();

        // Brand
        if (filter.getBrandId() != null) {
            conditions.add("brandId = :brandId");
            values.put(":brandId", AttributeValue.builder().s(filter.getBrandId()).build());
        }
        
        if (filter.getBrandName() != null) {
            conditions.add("brandName = :brandName");
            values.put(":brandName", AttributeValue.builder().s(filter.getBrandName()).build());
        }

        // Price range
        if (filter.getMinPrice() != null) {
            conditions.add("salePrice >= :minPrice");
            values.put(":minPrice", AttributeValue.builder().n(filter.getMinPrice().toString()).build());
        }
        if (filter.getMaxPrice() != null) {
            conditions.add("salePrice <= :maxPrice");
            values.put(":maxPrice", AttributeValue.builder().n(filter.getMaxPrice().toString()).build());
        }

        // Gender
        if (filter.getGender() != null) {
            conditions.add("gender = :gender");
            values.put(":gender", AttributeValue.builder().s(filter.getGender()).build());
        }

        // Material (contains)
        if (filter.getMaterial() != null) {
            conditions.add("contains(material, :material)");
            values.put(":material", AttributeValue.builder().s(filter.getMaterial()).build());
        }

        // Active
        if (Boolean.TRUE.equals(filter.getIsActive())) {
            conditions.add("isActive = :active");
            values.put(":active", AttributeValue.builder().bool(true).build());
        }

        // On sale
        if (Boolean.TRUE.equals(filter.getOnSale())) {
            conditions.add("salePrice < basePrice");
        }

        // Multi-value filters (sizes, colors) - using OR + contains
        if (filter.getSizes() != null && !filter.getSizes().isEmpty()) {
            List<String> sizeConditions = filter.getSizes().stream()
                .map(s -> "contains(availableSizes, :size_" + s + ")")
                .collect(Collectors.toList());
            conditions.add("(" + String.join(" OR ", sizeConditions) + ")");
            int i = 0;
            for (String size_ : filter.getSizes()) {
                values.put(":size_" + size_, AttributeValue.builder().s(size_).build());
                i++;
            }
        }

        if (filter.getColors() != null && !filter.getColors().isEmpty()) {
            List<String> colorConditions = filter.getColors().stream()
                .map(c -> "contains(availableColors, :color_" + c + ")")
                .collect(Collectors.toList());
            conditions.add("(" + String.join(" OR ", colorConditions) + ")");
            int i = 0;
            for (String color : filter.getColors()) {
                values.put(":color_" + color, AttributeValue.builder().s(color).build());
                i++;
            }
        }

        // Apply filter expression if any conditions exist
        if (!conditions.isEmpty()) {
            String filterExpr = String.join(" AND ", conditions);
            exprBuilder.expression(filterExpr).expressionValues(values);
            scanBuilder.filterExpression(exprBuilder.build());
        }

        // Execute scan
        return Flux.from(productTable.scan(scanBuilder.build()).items())
            .sort(getComparator(filter.getSortBy()))      // in-memory sort
            .skip((long) page * size)                     // in-memory pagination
            .take(size);
    }

    /**
     * When categoryId is provided → first get product IDs from mapping table,
     * then fetch products and apply remaining filters
     */
    private Flux<Product> filterByCategoryWithAdditionalFilters(ProductFilterDto filter) {
        String categoryId = filter.getCategoryId();

        // Get all product IDs in this category
        return mappingRepository.findByCategoryId(categoryId)
            .map(ProductCategoryMapping::getProductId)
            .collectList()
            .flatMapMany(productIds -> {
                if (productIds.isEmpty()) {
                    return Flux.empty();
                }

                // Now fetch products with additional filters
                ScanEnhancedRequest.Builder scanBuilder = ScanEnhancedRequest.builder();

                Expression.Builder exprBuilder = Expression.builder();
                List<String> conditions = new ArrayList<>();
                Map<String, AttributeValue> values = new HashMap<>();

                // Product ID IN (...) 
                if (!productIds.isEmpty()) {
                    List<AttributeValue> idValues = productIds.stream()
                        .map(id -> AttributeValue.builder().s(id).build())
                        .collect(Collectors.toList());
                    conditions.add("productId IN (" + 
                        IntStream.range(0, idValues.size())
                            .mapToObj(i -> ":id" + i)
                            .collect(Collectors.joining(", ")) + ")");
                    for (int i = 0; i < idValues.size(); i++) {
                        values.put(":id" + i, idValues.get(i));
                    }
                }

                // Apply remaining filters (brand, price, gender, etc.) - same as above
                if (filter.getBrandId() != null) {
                    conditions.add("brandId = :brandId");
                    values.put(":brandId", AttributeValue.builder().s(filter.getBrandId()).build());
                }
                
                if (filter.getBrandName() != null) {
                    conditions.add("brandName = :brandName");
                    values.put(":brandName", AttributeValue.builder().s(filter.getBrandName()).build());
                }
                
                // Price range
                if (filter.getMinPrice() != null) {
                    conditions.add("salePrice >= :minPrice");
                    values.put(":minPrice", AttributeValue.builder().n(filter.getMinPrice().toString()).build());
                }
                if (filter.getMaxPrice() != null) {
                    conditions.add("salePrice <= :maxPrice");
                    values.put(":maxPrice", AttributeValue.builder().n(filter.getMaxPrice().toString()).build());
                }

                // Gender
                if (filter.getGender() != null) {
                    conditions.add("gender = :gender");
                    values.put(":gender", AttributeValue.builder().s(filter.getGender()).build());
                }

                // Material (contains)
                if (filter.getMaterial() != null) {
                    conditions.add("contains(material, :material)");
                    values.put(":material", AttributeValue.builder().s(filter.getMaterial()).build());
                }

                // Active
                if (Boolean.TRUE.equals(filter.getIsActive())) {
                    conditions.add("isActive = :active");
                    values.put(":active", AttributeValue.builder().bool(true).build());
                }

                // On sale
                if (Boolean.TRUE.equals(filter.getOnSale())) {
                    conditions.add("salePrice < basePrice");
                }
                
             // Multi-value filters (sizes, colors) - using OR + contains
                if (filter.getSizes() != null && !filter.getSizes().isEmpty()) {
                    List<String> sizeConditions = filter.getSizes().stream()
                        .map(s -> "contains(availableSizes, :size_" + s + ")")
                        .collect(Collectors.toList());
                    conditions.add("(" + String.join(" OR ", sizeConditions) + ")");
                    int i = 0;
                    for (String size_ : filter.getSizes()) {
                        values.put(":size_" + size_, AttributeValue.builder().s(size_).build());
                        i++;
                    }
                }

                if (filter.getColors() != null && !filter.getColors().isEmpty()) {
                    List<String> colorConditions = filter.getColors().stream()
                        .map(c -> "contains(availableColors, :color_" + c + ")")
                        .collect(Collectors.toList());
                    conditions.add("(" + String.join(" OR ", colorConditions) + ")");
                    int i = 0;
                    for (String color : filter.getColors()) {
                        values.put(":color_" + color, AttributeValue.builder().s(color).build());
                        i++;
                    }
                }

                if (!conditions.isEmpty()) {
                    String filterExpr = String.join(" AND ", conditions);
                    exprBuilder.expression(filterExpr).expressionValues(values);
                    scanBuilder.filterExpression(exprBuilder.build());
                }

                return Flux.from(productTable.scan(scanBuilder.build()).items())
                    .sort(getComparator(filter.getSortBy()))
                    .skip((long) filter.getPage() * filter.getSize())
                    .take(filter.getSize());
            });
    }

    // Helper: category filter using mapping table (recommended)
    private Flux<Product> filterByCategory(String categoryId) {
        // Step 1: Get product IDs from mapping
        return mappingRepository.findByCategoryId(categoryId)
            .map(ProductCategoryMapping::getProductId)
            .flatMap(productId -> findById(productId));
    }

    private Comparator<Product> getComparator(String sortBy) {
        if (sortBy == null) return Comparator.comparing(Product::getCreatedAt).reversed();

        return switch (sortBy) {
            case "price_asc" -> Comparator.comparing(Product::getSalePrice);
            case "price_desc" -> Comparator.comparing(Product::getSalePrice).reversed();
            case "rating_desc" -> Comparator.comparing(Product::getRatingAverage).reversed();
            case "newest" -> Comparator.comparing(Product::getCreatedAt).reversed();
            default -> Comparator.comparing(Product::getCreatedAt).reversed();
        };
    }
    
    /**
     * Get filter options for a category (brands, sizes, colors, price range, etc.)
     */
    public Mono<CategoryFilterOptionsDto> getFilterOptionsByCategory(String categoryId) {
        // Step 1: Get all product IDs in this category
        return mappingRepository.findByCategoryId(categoryId)
            .map(ProductCategoryMapping::getProductId)
            .collectList()
            .flatMap(productIds -> {
                if (productIds.isEmpty()) {
                    return Mono.just(CategoryFilterOptionsDto.builder()
                        .categoryId(categoryId)
                        .productCount(0L)
                        .build());
                }

                // Step 2: Fetch all matching products (batch get or scan with filter)
                return Flux.fromIterable(productIds)
                    .flatMap(this::findById)
                    .collectList()
                    .map(products -> buildFilterOptions(categoryId, products));
            });
    }

    private CategoryFilterOptionsDto buildFilterOptions(String categoryId, List<Product> products) {
        long count = products.size();

        // Brands
        Map<String, String> brandNameBrandIdMap = new HashMap<>();
        Map<String, Long> brandCounts = products.stream()
            .filter(p -> p.getBrandName() != null && p.getBrandId() != null)
            .map(p -> {
            	brandNameBrandIdMap.put(p.getBrandName(), p.getBrandId());
            	return p;
            })
            .collect(Collectors.groupingBy(Product::getBrandName, Collectors.counting()));

        List<CategoryFilterOptionsDto.FilterOption> brands = brandCounts.entrySet().stream()
            .map(e -> CategoryFilterOptionsDto.FilterOption.builder()
                .value(e.getKey())
                //.label(e.getKey() + " (" + e.getValue() + ")")
                .label(e.getKey())
                .count(e.getValue())
                .build())
            .sorted(Comparator.comparing(CategoryFilterOptionsDto.FilterOption::getLabel))
            .collect(Collectors.toList());

        // Sizes (flatten all availableSizes)
        Map<String, Long> sizeCounts = products.stream()
            .flatMap(p -> p.getAvailableSizes().stream())
            .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        List<CategoryFilterOptionsDto.FilterOption> sizes = sizeCounts.entrySet().stream()
            .map(e -> CategoryFilterOptionsDto.FilterOption.builder()
                .value(e.getKey())
                .label(e.getKey() + " (" + e.getValue() + ")")
                .count(e.getValue())
                .build())
            .sorted(Comparator.comparing(CategoryFilterOptionsDto.FilterOption::getValue))
            .collect(Collectors.toList());

        // Colors (similar to sizes)
        Map<String, Long> colorCounts = products.stream()
            .flatMap(p -> p.getAvailableColors().stream())
            .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        List<CategoryFilterOptionsDto.FilterOption> colors = colorCounts.entrySet().stream()
            .map(e -> CategoryFilterOptionsDto.FilterOption.builder()
                .value(e.getKey())
                .label(e.getKey() + " (" + e.getValue() + ")")
                .count(e.getValue())
                .build())
            .sorted(Comparator.comparing(CategoryFilterOptionsDto.FilterOption::getValue))
            .collect(Collectors.toList());

        // Price range
        double minPrice = products.stream()
            .mapToDouble(Product::getSalePrice)
            .min().orElse(0.0);
        double maxPrice = products.stream()
            .mapToDouble(Product::getSalePrice)
            .max().orElse(0.0);

        // Optional: predefined price buckets (customize as needed)
        List<CategoryFilterOptionsDto.PriceBucket> priceBuckets = List.of(
            CategoryFilterOptionsDto.PriceBucket.builder().range("0-500").count(0L).build(),
            CategoryFilterOptionsDto.PriceBucket.builder().range("500-1000").count(0L).build()
            // Add more buckets...
        );

        // Material, fitType, gender - similar grouping
        // ... (implement similarly if needed)

        return CategoryFilterOptionsDto.builder()
            .categoryId(categoryId)
            .productCount(count)
            .brands(brands)
            .sizes(sizes)
            .colors(colors)
            .priceRange(CategoryFilterOptionsDto.PriceRange.builder()
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .buckets(priceBuckets)
                .build())
            .build();
    }
}
