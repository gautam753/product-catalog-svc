package com.mcart.productcatalogsvc.repository;

import com.mcart.productcatalogsvc.entity.ProductCategoryMapping;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.enhanced.dynamodb.Key;

@Repository
public class ProductCategoryMappingRepository {

    private final DynamoDbAsyncTable<ProductCategoryMapping> mappingTable;

    public ProductCategoryMappingRepository(DynamoDbEnhancedAsyncClient enhancedClient) {

        this.mappingTable =
                enhancedClient.table(
                        "mcart-ProductCategoryMapping",
                        TableSchema.fromBean(ProductCategoryMapping.class)
                );
    }

    // ==================================================
    // Find all products under a category (PK Query)
    // PK = categoryId
    // SK = productId
    // ==================================================
    public Flux<ProductCategoryMapping> findByCategoryId(String categoryId) {

        return Flux.from(
                mappingTable.query(r -> r.queryConditional(
                        QueryConditional.keyEqualTo(
                                Key.builder()
                                        .partitionValue(categoryId)
                                        .build()
                        )
                ))
        )
        .flatMapIterable(Page::items);
    }

    // ==================================================
    // Find all categories for a product (GSI)
    // GSI_ProductToCategories
    // PK = productId
    // SK = categoryId
    // ==================================================
    public Flux<ProductCategoryMapping> findByProductId(String productId) {

        return Flux.from(
                mappingTable.index("GSI_ProductToCategories")
                        .query(r -> r.queryConditional(
                                QueryConditional.keyEqualTo(
                                        Key.builder()
                                                .partitionValue(productId)
                                                .build()
                                )
                        ))
        )
        .flatMapIterable(Page::items);
    }

    // ==================================================
    // Find single mapping (PK + SK)
    // ==================================================
    public Mono<ProductCategoryMapping> findByCompositeKey(String categoryId, String productId) {

        return Mono.fromFuture(
                mappingTable.getItem(r -> r.key(
                        Key.builder()
                                .partitionValue(categoryId)
                                .sortValue(productId)
                                .build()
                ))
        );
    }

    // ==================================================
    // Save
    // ==================================================
    public Mono<Void> save(ProductCategoryMapping mapping) {

        return Mono.fromFuture(mappingTable.putItem(mapping))
                .then();
    }

    // ==================================================
    // Delete (PK + SK)
    // ==================================================
    public Mono<Void> delete(String categoryId, String productId) {

        return Mono.fromFuture(
                mappingTable.deleteItem(r -> r.key(
                        Key.builder()
                                .partitionValue(categoryId)
                                .sortValue(productId)
                                .build()
                ))
        ).then();
    }
}
