package com.ecommerceproject.orderservice.services;

import com.ecommerceproject.orderservice.dtos.requestdto.CreateOrderRequestDto;
import com.ecommerceproject.orderservice.dtos.responsedto.CreateOrderResponseDto;
import com.ecommerceproject.orderservice.dtos.responsedto.GetOrderResponseDto;
import com.ecommerceproject.orderservice.models.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IOrderService {

    CreateOrderResponseDto createOrder(Long userId, CreateOrderRequestDto createOrderRequestDto);

    GetOrderResponseDto getOrderById(Long userId, Long orderId);

    Page<GetOrderResponseDto> getAllOrders(Long orderId, Pageable pageable);

    Order findOrderByUserId(Long userId);

    GetOrderResponseDto shipOrder(Long userId, Long orderId);

    GetOrderResponseDto deliverOrder(Long userId, Long orderId);

    GetOrderResponseDto cancelOrder(Long userId, Long orderId);

}
