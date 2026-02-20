package com.mcart.productcatalogsvc.entity;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.util.ArrayList;
import java.util.List;

@Data
@DynamoDbBean
public class Product {

    private String productId;           // Partition Key
    private String name;
    private String slug;
    private String description;
    private String brandId;
    private String brandName;
    private Double basePrice;
    private Double salePrice;
    private String currency = "INR";
    private Integer discountPercentage;
    private Double ratingAverage;
    private Integer ratingCount;
    private String primaryImage;
    private List<String> images = new ArrayList<>();
    private List<String> availableSizes = new ArrayList<>();
    private List<String> availableColors = new ArrayList<>();
    private String material;
    private String fitType;
    private String gender;
    private Boolean isActive = true;
    private Boolean isReturnable = true;
    private Boolean isCODAvailable = true;
    private String createdAt;
    private String updatedAt;

    @DynamoDbPartitionKey
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    @DynamoDbSecondaryPartitionKey(indexNames = "GSI_ProductBySlug")
    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    @DynamoDbSecondaryPartitionKey(indexNames = "GSI_ProductByBrand")
    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }
}
