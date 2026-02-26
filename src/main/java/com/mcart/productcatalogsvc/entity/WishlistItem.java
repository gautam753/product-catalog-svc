package com.mcart.productcatalogsvc.entity;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Data
@DynamoDbBean
public class WishlistItem {

    private String PK;           // USER#<userId>
    private String SK;           // ITEM#<productId>#<variantId> or ITEM#<productId>
    private String productId;
    private String variantId;    // optional
    private String priority;     // low/medium/high
    private String notes;
    private String addedAt;
    private String updatedAt;

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
