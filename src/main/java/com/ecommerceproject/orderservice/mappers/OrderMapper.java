package com.ecommerceproject.orderservice.mappers;

import com.ecommerceproject.orderservice.dtos.responsedto.GetOrderResponseDto;
import com.ecommerceproject.orderservice.dtos.responsedto.OrderItemResponseDto;
import com.ecommerceproject.orderservice.models.Order;
import com.ecommerceproject.orderservice.models.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderMapper {

    public GetOrderResponseDto toGetOrderResponseDto(Order order) {

        GetOrderResponseDto response = new GetOrderResponseDto();

        response.setOrderId(order.getOrderId());
        response.setTotalAmount(order.getTotalAmount());
        response.setOrderStatus(order.getOrderStatus());

        List<OrderItemResponseDto> items = order.getItemList()
                .stream()
                .map(this::toOrderItemResponseDto)
                .toList();

        response.setItems(items);

        return response;
    }

    private OrderItemResponseDto toOrderItemResponseDto(OrderItem orderItem) {

        OrderItemResponseDto response = new OrderItemResponseDto();

        response.setProductId(orderItem.getProductId());
        response.setProductName(orderItem.getName());
        response.setQuantity(orderItem.getQuantity());
        response.setPrice(orderItem.getPrice());

        BigDecimal subtotal = orderItem.getPrice()
                .multiply(BigDecimal.valueOf(orderItem.getQuantity()));

        response.setSubtotal(subtotal);

        return response;
    }
}