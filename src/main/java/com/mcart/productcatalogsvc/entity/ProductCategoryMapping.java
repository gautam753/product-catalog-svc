package com.mcart.productcatalogsvc.entity;

//com.mcart.productcatalogsvc.model.ProductCategoryMapping.java
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Data
@DynamoDbBean
public class ProductCategoryMapping {

 private String categoryId;     // Partition Key (HASH)
 private String productId;      // Sort Key (RANGE)
 private String createdAt;
 private String updatedAt;

 @DynamoDbPartitionKey
 public String getCategoryId() {
     return categoryId;
 }

 public void setCategoryId(String categoryId) {
     this.categoryId = categoryId;
 }

 @DynamoDbSortKey
 public String getProductId() {
     return productId;
 }

 public void setProductId(String productId) {
     this.productId = productId;
 }
}