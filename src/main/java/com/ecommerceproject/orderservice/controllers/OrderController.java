package com.ecommerceproject.orderservice.controllers;

import com.ecommerceproject.orderservice.clients.ProductServiceClient;
import com.ecommerceproject.orderservice.dtos.requestdto.CreateOrderRequestDto;
import com.ecommerceproject.orderservice.dtos.responsedto.CreateOrderResponseDto;
import com.ecommerceproject.orderservice.dtos.responsedto.GetOrderResponseDto;
import com.ecommerceproject.orderservice.dtos.responsedto.ProductResponseDto;
import com.ecommerceproject.orderservice.gateways.ProductServiceGateway;
import com.ecommerceproject.orderservice.services.IOrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        // temporary until JWT integration
        Long userId = 1L;

        CreateOrderResponseDto response = orderService.createOrder(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<GetOrderResponseDto> getOrderById(@PathVariable Long orderId) {
        // Temporary until JWT integration
        Long userId = 1L;

        GetOrderResponseDto response = orderService.getOrderById(userId, orderId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-orders")
    public ResponseEntity<Page<GetOrderResponseDto>> getMyOrders(Pageable pageable) {
        // Temporary until JWT integration
        Long userId = 1L;

        Page<GetOrderResponseDto> response = orderService.getAllOrders(userId, pageable);

        return ResponseEntity.ok(response);
    }
}