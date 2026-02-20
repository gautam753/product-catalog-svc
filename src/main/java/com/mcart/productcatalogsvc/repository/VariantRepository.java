package com.mcart.productcatalogsvc.repository;

import com.mcart.productcatalogsvc.entity.Variant;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.enhanced.dynamodb.Key;

@Repository
public class VariantRepository {

    private final DynamoDbAsyncTable<Variant> variantTable;

    public VariantRepository(DynamoDbEnhancedAsyncClient enhancedClient) {
        this.variantTable =
                enhancedClient.table("mcart-Variants",
                        TableSchema.fromBean(Variant.class));
    }

    // ==========================================
    // Find Single Variant (PK + SK)
    // ==========================================
    public Mono<Variant> findById(String productId, String variantId) {

        return Mono.fromFuture(
                variantTable.getItem(r -> r.key(
                        Key.builder()
                                .partitionValue(productId)
                                .sortValue(variantId)
                                .build()
                ))
        );
    }

    // ==========================================
    // Find All Variants By ProductId (PK Query)
    // ==========================================
    public Flux<Variant> findByProductId(String productId) {

        return Flux.from(
                variantTable.query(r -> r.queryConditional(
                        QueryConditional.keyEqualTo(
                                Key.builder()
                                        .partitionValue(productId)
                                        .build()
                        )
                ))
        )
        .flatMapIterable(Page::items);
    }

    // ==========================================
    // Find Variant By SKU (GSI)
    // GSI_VariantBySKU (HASH = sku)
    // ==========================================
    public Mono<Variant> findBySku(String sku) {

        return Flux.from(
                variantTable.index("GSI_VariantBySKU")
                        .query(r -> r.queryConditional(
                                QueryConditional.keyEqualTo(
                                        Key.builder()
                                                .partitionValue(sku)
                                                .build()
                                )
                        ).limit(1))
        )
        .flatMapIterable(Page::items)
        .next();
    }

    // ==========================================
    // Low Stock (Scan + Filter)
    // NOTE: For production, better to use GSI
    // ==========================================
    public Flux<Variant> findLowStock(int threshold) {

        return findAll()
                .filter(v ->
                        v.getAvailableStock() != null &&
                        v.getAvailableStock() <= threshold
                );
    }

    // ==========================================
    // Scan All Variants
    // ==========================================
    public Flux<Variant> findAll() {

        return Flux.from(variantTable.scan())
                .flatMapIterable(Page::items);
    }

    // ==========================================
    // Save
    // ==========================================
    public Mono<Void> save(Variant variant) {

        return Mono.fromFuture(variantTable.putItem(variant))
                .then();
    }

    // ==========================================
    // Delete (PK + SK)
    // ==========================================
    public Mono<Void> delete(String productId, String variantId) {

        return Mono.fromFuture(
                variantTable.deleteItem(r -> r.key(
                        Key.builder()
                                .partitionValue(productId)
                                .sortValue(variantId)
                                .build()
                ))
        ).then();
    }
}
