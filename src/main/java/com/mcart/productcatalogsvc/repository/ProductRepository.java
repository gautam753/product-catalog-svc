package com.mcart.productcatalogsvc.repository;

import com.mcart.productcatalogsvc.entity.Product;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Repository
public class ProductRepository {

    private final DynamoDbAsyncTable<Product> productTable;

    public ProductRepository(DynamoDbEnhancedAsyncClient enhancedClient) {
        this.productTable = enhancedClient.table(
                "mcart-Products",
                TableSchema.fromBean(Product.class)
        );
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
}
