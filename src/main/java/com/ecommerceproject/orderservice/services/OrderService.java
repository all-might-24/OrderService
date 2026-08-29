package com.ecommerceproject.orderservice.services;

import com.ecommerceproject.orderservice.dtos.requestdto.CreateOrderRequestDto;
import com.ecommerceproject.orderservice.dtos.requestdto.OrderItemsRequestDto;
import com.ecommerceproject.orderservice.dtos.responsedto.CreateOrderResponseDto;
import com.ecommerceproject.orderservice.dtos.responsedto.GetOrderResponseDto;
import com.ecommerceproject.orderservice.dtos.responsedto.ProductResponseDto;
import com.ecommerceproject.orderservice.exceptions.InsufficientStockException;
import com.ecommerceproject.orderservice.exceptions.OrderNotFoundException;
import com.ecommerceproject.orderservice.gateways.ProductServiceGateway;
import com.ecommerceproject.orderservice.mappers.OrderMapper;
import com.ecommerceproject.orderservice.models.Order;
import com.ecommerceproject.orderservice.models.OrderItem;
import com.ecommerceproject.orderservice.models.enums.OrderStatus;
import com.ecommerceproject.orderservice.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderService implements IOrderService{

    private final OrderRepository orderRepository;

    private final OrderMapper orderMapper;

    private final ProductServiceGateway productServiceGateway;

    public OrderService(OrderRepository orderRepository,
                        OrderMapper orderMapper,
                        ProductServiceGateway productServiceGateway) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.productServiceGateway = productServiceGateway;
    }

    @Override
    @Transactional
    public CreateOrderResponseDto createOrder(Long userId, CreateOrderRequestDto createOrderRequestDto) {

        Order order = new Order();
        order.setUserId(userId);
        order.setOrderStatus(OrderStatus.CREATED);

        List<OrderItem> orderItems = new ArrayList<>();

        BigDecimal totalAmount = BigDecimal.ZERO;

        // To counter Duplicate order items to avoid multiple product service calls
        Map<Long, Integer> productEntries = new HashMap<>();

        for(OrderItemsRequestDto products : createOrderRequestDto.getOrderItems()) {
            productEntries.merge(products.getProductId(), products.getQuantity(), Integer::sum);
        }

        for (Map.Entry<Long, Integer> entry: productEntries.entrySet()) {

            Long productId = entry.getKey();
            Integer quantity = entry.getValue();


            ProductResponseDto product = productServiceGateway.getProductById(productId);

            if(product.getQty() < quantity) {
                throw new InsufficientStockException("Insufficient stock for product " + productId + ". Requested: " + quantity + ", available: " + product.getQty());
            }

            OrderItem orderItem = new OrderItem();

            orderItem.setProductId(product.getId());
            orderItem.setName(product.getTitle());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(quantity);

            orderItems.add(orderItem);

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));

            totalAmount = totalAmount.add(itemTotal);
        }
        order.setItemList(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        CreateOrderResponseDto response = new CreateOrderResponseDto();

        response.setOrderId(savedOrder.getOrderId());
        response.setTotalAmount(savedOrder.getTotalAmount());
        response.setOrderStatus(savedOrder.getOrderStatus());

        return response;
    }

    @Override
    public GetOrderResponseDto getOrderById(Long userId, Long orderId) {
        Order order = findOrderById(orderId);

        if(!order.getOrderId().equals(orderId)) {
            throw new OrderNotFoundException("Order not found with user_id : "  + userId);
        }

        return orderMapper.toGetOrderResponseDto(order);
    }

    @Override
    public Page<GetOrderResponseDto> getAllOrders(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);

        return orders.map(orderMapper::toGetOrderResponseDto);
    }

    @Override
    public Order findOrderByUserId(Long userId) {
        Optional<Order> optionalOrder = orderRepository.findByUserId(userId);

        if(optionalOrder.isEmpty()) {
            throw new OrderNotFoundException("Order not found with user_id : "  + userId);
        }
        return optionalOrder.get();
    }

    private Order findOrderById(Long orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);

        if(optionalOrder.isEmpty()) {
            throw new OrderNotFoundException("Order not found with order_id : "  + orderId);
        }
        return optionalOrder.get();
    }

}
