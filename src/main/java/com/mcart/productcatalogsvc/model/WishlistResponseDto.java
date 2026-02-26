package com.mcart.productcatalogsvc.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WishlistResponseDto {
    private String userId;
    private List<WishlistItemDto> items;
}
