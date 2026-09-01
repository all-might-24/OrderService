package com.ecommerceproject.orderservice.controllers;

import com.ecommerceproject.orderservice.dtos.requestdto.CreateOrderRequestDto;
import com.ecommerceproject.orderservice.dtos.responsedto.CreateOrderResponseDto;
import com.ecommerceproject.orderservice.dtos.responsedto.GetOrderResponseDto;

import com.ecommerceproject.orderservice.services.IOrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {


    private final IOrderService orderService;

    public OrderController(IOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<CreateOrderResponseDto> createOrder(@RequestBody @Valid CreateOrderRequestDto request) {

        Long userId = getAuthenticatedUserId();

        CreateOrderResponseDto response = orderService.createOrder(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<GetOrderResponseDto> getOrderById(@PathVariable Long orderId) {

        Long userId = getAuthenticatedUserId();

        GetOrderResponseDto response = orderService.getOrderById(userId, orderId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-orders")
    public ResponseEntity<Page<GetOrderResponseDto>> getMyOrders(Pageable pageable) {

        Long userId = getAuthenticatedUserId();

        Page<GetOrderResponseDto> response = orderService.getAllOrders(userId, pageable);

        return ResponseEntity.ok(response);
    }

    @PostMapping("{orderId}/ship")
    public ResponseEntity<GetOrderResponseDto> shipOrder(@PathVariable("orderId") Long orderId) {
        GetOrderResponseDto getOrderResponseDto = orderService.shipOrder(orderId);

        return ResponseEntity.ok(getOrderResponseDto);
    }

    @PostMapping("{orderId}/deliver")
    public ResponseEntity<GetOrderResponseDto> deliverOrder(@PathVariable("orderId") Long orderId) {

        GetOrderResponseDto getOrderResponseDto = orderService.deliverOrder(orderId);

        return ResponseEntity.ok(getOrderResponseDto);
    }

    @PostMapping("{orderId}/cancel")
    public ResponseEntity<GetOrderResponseDto> cancelOrder(@PathVariable("orderId") Long orderId) {
        Long userId = getAuthenticatedUserId();

        GetOrderResponseDto getOrderResponseDto = orderService.cancelOrder(userId, orderId);

        return ResponseEntity.ok(getOrderResponseDto);
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }
}