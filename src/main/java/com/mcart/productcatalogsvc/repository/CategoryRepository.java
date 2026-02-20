package com.mcart.productcatalogsvc.repository;

import com.mcart.productcatalogsvc.entity.Category;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.enhanced.dynamodb.Key;

@Repository
public class CategoryRepository {

    private final DynamoDbAsyncTable<Category> categoryTable;

    public CategoryRepository(DynamoDbEnhancedAsyncClient enhancedClient) {
        this.categoryTable =
                enhancedClient.table("mcart-Categories",
                        TableSchema.fromBean(Category.class));
    }

    // =============================
    // Find By ID
    // =============================
    public Mono<Category> findById(String categoryId) {

        return Mono.fromFuture(
                categoryTable.getItem(r -> r.key(
                        Key.builder()
                                .partitionValue(categoryId)
                                .build()
                ))
        );
    }

    // =============================
    // Find By Slug (GSI)
    // GSI: GSI_CategoryBySlug (HASH = slug)
    // =============================
    public Mono<Category> findBySlug(String slug) {

        return Flux.from(
                categoryTable.index("GSI_CategoryBySlug")
                        .query(r -> r.queryConditional(
                                QueryConditional.keyEqualTo(
                                        Key.builder()
                                                .partitionValue(slug)
                                                .build()
                                )
                        ).limit(1))
        )
        .flatMapIterable(Page::items)
        .next();
    }

    // =============================
    // Find All
    // =============================
    public Flux<Category> findAll() {

        return Flux.from(categoryTable.scan())
                .flatMapIterable(Page::items);
    }

    // =============================
    // Root Categories
    // =============================
    public Flux<Category> findRootCategories() {

        return findSubcategories("ROOT");
    }

    // =============================
    // Subcategories (GSI)
    // GSI: GSI_ParentCategory (HASH = parentCategoryId)
    // =============================
    public Flux<Category> findSubcategories(String parentCategoryId) {

        return Flux.from(
                categoryTable.index("GSI_ParentCategory")
                        .query(r -> r.queryConditional(
                                QueryConditional.keyEqualTo(
                                        Key.builder()
                                                .partitionValue(parentCategoryId)
                                                .build()
                                )
                        ))
        )
        .flatMapIterable(Page::items)
        .sort((c1, c2) -> Integer.compare(
                c1.getDisplayOrder() != null ? c1.getDisplayOrder() : 0,
                c2.getDisplayOrder() != null ? c2.getDisplayOrder() : 0
        ));
    }

    // =============================
    // Save
    // =============================
    public Mono<Void> save(Category category) {

        return Mono.fromFuture(categoryTable.putItem(category))
                .then();
    }

    // =============================
    // Delete
    // =============================
    public Mono<Void> deleteById(String categoryId) {

        return Mono.fromFuture(
                categoryTable.deleteItem(r -> r.key(
                        Key.builder()
                                .partitionValue(categoryId)
                                .build()
                ))
        ).then();
    }
}
