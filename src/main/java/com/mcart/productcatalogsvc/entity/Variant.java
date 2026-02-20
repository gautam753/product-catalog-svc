package com.mcart.productcatalogsvc.entity;

// com.mcart.productcatalogsvc.model.Variant.java
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.util.Objects;

@Data
@DynamoDbBean
public class Variant {

    private String productId;           // Partition Key
    private String variantId;           // Sort Key
    private String color;
    private String size;
    private String sku;
    private Double price;
    private Double mrp;
    private Integer stockQuantity;
    private Integer reservedStock;
    private Integer availableStock;
    private String barcode;
    private String weight;
    private String dimensions;
    private String createdAt;
    private String updatedAt;

    @DynamoDbPartitionKey
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @DynamoDbSortKey
    public String getVariantId() {
        return variantId;
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
    }
    
    @DynamoDbSecondaryPartitionKey(indexNames = "GSI_VariantBySKU")
    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }
}
