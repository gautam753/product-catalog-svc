package com.mcart.productcatalogsvc.entity;

//com.mcart.productcatalogsvc.model.Category.java
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.util.ArrayList;
import java.util.List;

@Data
@DynamoDbBean
public class Category {

 private String categoryId;          // PK
 private String name;
 private String slug;
 private String parentCategoryId;    // null for root
 private Integer level;
 private Boolean isLeaf;
 private Integer displayOrder;
 private String description;
 private String image;
 private String createdAt;
 private String updatedAt;

 @DynamoDbPartitionKey
 public String getCategoryId() {
     return categoryId;
 }

 public void setCategoryId(String categoryId) {
     this.categoryId = categoryId;
 }
 
 @DynamoDbSecondaryPartitionKey(indexNames = "GSI_ParentCategory")
 public String getParentCategoryId() {
     return parentCategoryId;
 }

 public void setParentCategoryId(String parentCategoryId) {
     this.parentCategoryId = parentCategoryId;
 }
}