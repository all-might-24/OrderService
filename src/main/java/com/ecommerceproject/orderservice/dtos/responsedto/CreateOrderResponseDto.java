package com.ecommerceproject.orderservice.dtos.responsedto;

import com.ecommerceproject.orderservice.models.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateOrderResponseDto {
    private Long orderId;
    private BigDecimal totalAmount;
    private OrderStatus orderStatus;
}
