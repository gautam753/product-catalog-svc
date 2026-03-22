package com.mcart.productcatalogsvc.orders.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

import com.mcart.productcatalogsvc.orders.model.AddressSnapshot;
import com.mcart.productcatalogsvc.orders.model.OrderItem;

@Data
@Builder
public class OrderResponse {
    private String orderId;
    private String userId;
    private String status;
    private String paymentMethod;
    private String addressId;
    private AddressSnapshot addressSnapshot;
    private List<OrderItem> items;
    private Integer totalItems;
    private Double totalAmount;
    private String currency;
    private String notes;
    private String createdAt;
    private String updatedAt;
}