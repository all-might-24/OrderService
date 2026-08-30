package com.ecommerceproject.orderservice.dtos.responsedto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryResponseDto {
    private Long productId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
}
