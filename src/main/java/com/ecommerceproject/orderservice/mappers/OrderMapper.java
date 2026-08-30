package com.ecommerceproject.orderservice.mappers;

import com.ecommerceproject.orderservice.dtos.responsedto.CreateOrderResponseDto;
import com.ecommerceproject.orderservice.dtos.responsedto.GetOrderResponseDto;
import com.ecommerceproject.orderservice.dtos.responsedto.OrderItemResponseDto;
import com.ecommerceproject.orderservice.models.Order;
import com.ecommerceproject.orderservice.models.OrderItem;
import com.ecommerceproject.orderservice.models.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderMapper {

    public GetOrderResponseDto toGetOrderResponseDto(Order order) {

        GetOrderResponseDto response = new GetOrderResponseDto();

        response.setOrderId(order.getId());
        response.setTotalAmount(order.getTotalAmount());
        response.setOrderStatus(order.getOrderStatus());
        response.setUserId(order.getUserId());

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

    public CreateOrderResponseDto toCreateOrderResponseDto(Order order) {

        CreateOrderResponseDto responseDto = new CreateOrderResponseDto();

        responseDto.setOrderId(order.getId());
        responseDto.setTotalAmount(order.getTotalAmount());
        responseDto.setOrderStatus(order.getOrderStatus());

        return responseDto;
    }
}