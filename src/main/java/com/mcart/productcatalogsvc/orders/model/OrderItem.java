package com.mcart.productcatalogsvc.orders.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class OrderItem {
    private String productId;
    private String variantId;
    private Integer quantity;
    private Double priceAtOrder;
    private String productName;
    private String brandName;
    private String primaryImage;
    private String size;
    private String color;
}