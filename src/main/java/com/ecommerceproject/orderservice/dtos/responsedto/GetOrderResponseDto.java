package com.ecommerceproject.orderservice.dtos.responsedto;

import com.ecommerceproject.orderservice.models.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class GetOrderResponseDto {
    private Long orderId;
    private Long userId;
    private BigDecimal totalAmount;
    private OrderStatus orderStatus;
    private List<OrderItemResponseDto> items;
}
