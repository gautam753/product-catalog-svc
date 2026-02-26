package com.mcart.productcatalogsvc.entity;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Data
@DynamoDbBean
public class CartItem {

    private String PK;           // USER#<userId> or GUEST#<token>
    private String SK;           // CART#<id> or ITEM#<productId>#<variantId>
    private String productId;
    private String variantId;
    private Integer quantity;
    private Double priceAtAddition;
    private String addedAt;
    private String updatedAt;
    private Long expiresAt;      // TTL for guest carts (Unix epoch seconds)

    @DynamoDbPartitionKey
    public String getPK() {
        return PK;
    }

    public void setPK(String PK) {
        this.PK = PK;
    }

    @DynamoDbSortKey
    public String getSK() {
        return SK;
    }

    public void setSK(String SK) {
        this.SK = SK;
    }
}
