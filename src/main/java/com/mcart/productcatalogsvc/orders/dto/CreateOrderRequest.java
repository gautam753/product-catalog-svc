package com.mcart.productcatalogsvc.orders.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateOrderRequest {

    @NotBlank(message = "addressId is required")
    private String addressId;

    @NotBlank(message = "paymentMethod is required")
    private String paymentMethod;   // COD, ONLINE

    private String notes;
}