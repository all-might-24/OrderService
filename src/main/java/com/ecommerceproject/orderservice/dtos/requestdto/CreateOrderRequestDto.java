package com.ecommerceproject.orderservice.dtos.requestdto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateOrderRequestDto {

    @NotEmpty(message = "order must contain at least one item")
    @Valid
    private List<OrderItemsRequestDto> orderItems;
}
