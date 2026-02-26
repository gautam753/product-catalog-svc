package com.mcart.productcatalogsvc.repository;

import org.springframework.stereotype.Repository;

import com.mcart.productcatalogsvc.entity.WishlistItem;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.*;

@Repository
public class WishlistRepository {

 private final DynamoDbAsyncTable<WishlistItem> wishlistTable;

 public WishlistRepository(DynamoDbEnhancedAsyncClient enhancedClient) {
     this.wishlistTable = enhancedClient.table("mcart-Wishlists", TableSchema.fromBean(WishlistItem.class));
 }

 public Mono<Void> save(WishlistItem item) {
     return Mono.fromFuture(wishlistTable.putItem(item));
 }

 public Mono<Void> deleteItem(String pk, String sk) {
     return Mono.fromFuture(
         wishlistTable.deleteItem(DeleteItemEnhancedRequest.builder()
        	     .key(k -> k.partitionValue(pk).sortValue(sk))
        	     .build())
     ).then();
 }

	public Flux<WishlistItem> findByPk(String pk) {
		return Flux.from(wishlistTable.query(QueryEnhancedRequest.builder()
				.queryConditional(QueryConditional.keyEqualTo(Key.builder().partitionValue(pk).build())).build()))
				.flatMap(page -> Flux.fromIterable(page.items()));
	}
}
