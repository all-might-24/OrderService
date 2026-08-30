package com.ecommerceproject.orderservice.dtos.requestdto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReleaseInventoryRequestDto {
    @NotNull(message = "product id cannot be null")
    @Positive(message = "product id cannot be negative")
    private Long productId;

    @NotNull(message = "quantity cannot be null")
    @Positive(message = "quantity should be greater than 0")
    private Integer quantity;
}
