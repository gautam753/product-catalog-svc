package com.mcart.productcatalogsvc.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartResponseDto {
    private String cartId; // or guestToken / userId
    private List<CartItemDto> items;
    private Integer totalItems;
    private Double totalPrice;
    private String currency;
}
