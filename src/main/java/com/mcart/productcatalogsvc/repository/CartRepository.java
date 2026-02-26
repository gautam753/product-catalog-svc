package com.mcart.productcatalogsvc.repository;

//com.mcart.productcatalogsvc.repository.CartRepository.java
import org.springframework.stereotype.Repository;

import com.mcart.productcatalogsvc.entity.CartItem;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.*;

@Repository
public class CartRepository {

 private final DynamoDbAsyncTable<com.mcart.productcatalogsvc.entity.CartItem> cartTable;

 public CartRepository(DynamoDbEnhancedAsyncClient enhancedClient) {
     this.cartTable = enhancedClient.table("mcart-Carts", TableSchema.fromBean(CartItem.class));
 }

 public Mono<Void> save(CartItem item) {
     return Mono.fromFuture(cartTable.putItem(item));
 }

 public Mono<Void> deleteItem(String pk, String sk) {
     return Mono.fromFuture(
         cartTable.deleteItem(DeleteItemEnhancedRequest.builder()
             .key(k -> k.partitionValue(pk).sortValue(sk))
             .build())
     ).then();
 }

 public Flux<CartItem> findItemsByPk(String pk) {
     return Flux.from(cartTable.query(
    		 QueryEnhancedRequest.builder()
				.queryConditional(QueryConditional.keyEqualTo(Key.builder().partitionValue(pk).build()))
				.build())
    		 ).flatMap(page -> Flux.fromIterable(page.items()));
 }

 public Mono<Void> deleteAllByPk(String pk) {
     return findItemsByPk(pk)
         .flatMap(item -> deleteItem(item.getPK(), item.getSK()))
         .then();
 }
}
