package com.mcart.productcatalogsvc.repository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.mcart.productcatalogsvc.orders.model.Order;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final DynamoDbEnhancedAsyncClient enhancedClient;

    private DynamoDbAsyncTable<Order> table;

    @PostConstruct
    public void init() {
        this.table = enhancedClient.table("mcart-orders", TableSchema.fromBean(Order.class));
    }

    // Save / update order
    public Mono<Order> save(Order order) {
        return Mono.fromFuture(table.putItem(order))
                .thenReturn(order);
    }

    // Get order by orderId
    public Mono<Order> findByOrderId(String orderId) {
        Key key = Key.builder().partitionValue(orderId).build();
        return Mono.fromFuture(table.getItem(key));
    }

    // Get all orders for a user — newest first
    public Flux<Order> findByUserId(String userId) {
        var index = table.index("userId-createdAt-index");
        var queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(userId).build()
        );
        var request = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .scanIndexForward(false)  // DESC — newest first
                .build();

        return Flux.from(index.query(request))
                .flatMap(page -> Flux.fromIterable(page.items()));
    }

    // Get orders by userId and status
    public Flux<Order> findByUserIdAndStatus(String userId, String status) {
        var index = table.index("userId-status-index");
        var queryConditional = QueryConditional.keyEqualTo(
                Key.builder()
                        .partitionValue(userId)
                        .sortValue(status)
                        .build()
        );
        return Flux.from(index.query(queryConditional))
                .flatMap(page -> Flux.fromIterable(page.items()));
    }

    // Delete order
    public Mono<Void> deleteByOrderId(String orderId) {
        Key key = Key.builder().partitionValue(orderId).build();
        return Mono.fromFuture(table.deleteItem(key)).then();
    }
}