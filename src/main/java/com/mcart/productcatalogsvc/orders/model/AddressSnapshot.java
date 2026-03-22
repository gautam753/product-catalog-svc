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
public class AddressSnapshot {
    private String name;
    private String phone;
    private String street;
    private String area;
    private String city;
    private String state;
    private String pincode;
    private String type;
}