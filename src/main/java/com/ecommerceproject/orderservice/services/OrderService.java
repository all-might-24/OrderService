package com.ecommerceproject.orderservice.services;

import com.ecommerceproject.orderservice.dtos.requestdto.CreateOrderRequestDto;
import com.ecommerceproject.orderservice.dtos.requestdto.OrderItemsRequestDto;
import com.ecommerceproject.orderservice.dtos.responsedto.CreateOrderResponseDto;
import com.ecommerceproject.orderservice.dtos.responsedto.GetOrderResponseDto;
import com.ecommerceproject.orderservice.dtos.responsedto.ProductResponseDto;
import com.ecommerceproject.orderservice.exceptions.InsufficientStockException;
import com.ecommerceproject.orderservice.exceptions.InvalidOrderStateException;
import com.ecommerceproject.orderservice.exceptions.OrderNotFoundException;
import com.ecommerceproject.orderservice.gateways.InventoryServiceGateway;
import com.ecommerceproject.orderservice.gateways.ProductServiceGateway;
import com.ecommerceproject.orderservice.mappers.OrderMapper;
import com.ecommerceproject.orderservice.models.Order;
import com.ecommerceproject.orderservice.models.OrderItem;
import com.ecommerceproject.orderservice.models.enums.OrderStatus;
import com.ecommerceproject.orderservice.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final InventoryServiceGateway inventoryServiceGateway;

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public OrderService(OrderRepository orderRepository,
                        OrderMapper orderMapper,
                        ProductServiceGateway productServiceGateway,
                        InventoryServiceGateway inventoryServiceGateway) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.productServiceGateway = productServiceGateway;
        this.inventoryServiceGateway = inventoryServiceGateway;
    }

    @Override
    @Transactional
    public CreateOrderResponseDto createOrder(Long userId, CreateOrderRequestDto createOrderRequestDto) {

        Order order = new Order();
        order.setUserId(userId);
        order.setOrderStatus(OrderStatus.CREATED);

        BigDecimal totalAmount = BigDecimal.ZERO;

        String requestId = UUID.randomUUID().toString();
        // Consolidating duplicate product entries
        Map<Long, Integer> productQuantities = new LinkedHashMap<>();

        for (OrderItemsRequestDto requestItem : createOrderRequestDto.getOrderItems()) {

            productQuantities.merge(
                    requestItem.getProductId(),
                    requestItem.getQuantity(),
                    Integer::sum
            );
        }

        // Keeping track of reservations that actually succeeded
        Map<Long, Integer> successfulReservations = new HashMap<>();

        try {
            for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {

                Long productId = entry.getKey();
                Integer requestedQuantity = entry.getValue();

                // ProductService is used for product details
                ProductResponseDto product = productServiceGateway.getProductById(productId);

                String operationId = "reserve-" + requestId + "-" + productId;
                // InventoryService is used for stock
                inventoryServiceGateway.reserveProduct(productId, requestedQuantity, operationId);

                // Reservation succeeded
                successfulReservations.put(productId, requestedQuantity);

                OrderItem orderItem = new OrderItem();

                orderItem.setProductId(product.getId());
                orderItem.setName(product.getTitle());
                orderItem.setPrice(product.getPrice());
                orderItem.setQuantity(requestedQuantity);

                order.addOrderItem(orderItem);

                BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(requestedQuantity));

                totalAmount = totalAmount.add(itemTotal);
            }

            order.setTotalAmount(totalAmount);

            Order savedOrder = orderRepository.save(order);

            return orderMapper.toCreateOrderResponseDto(savedOrder);

        } catch (Exception exception) {

            // Compensate reservations that already succeeded
            for (Map.Entry<Long, Integer> reservation : successfulReservations.entrySet()) {

                try {
                    String operationId = "release-" + requestId + "-" + reservation.getKey();
                    inventoryServiceGateway.releaseProduct(reservation.getKey(), reservation.getValue(), operationId);
                } catch (Exception releaseException) {
                    log.error(
                            "Failed to compensate inventory reservation. productId={}, quantity={}, requestId={}",
                            reservation.getKey(),
                            reservation.getValue(),
                            requestId,
                            releaseException
                    );
                }
            }
            throw exception;
        }
    }

    @Override
    public GetOrderResponseDto getOrderById(Long userId, Long orderId) {
        Order order = findOrderById(orderId);

        if (!order.getUserId().equals(userId)) {
            throw new OrderNotFoundException("Order not found with order_id : " + orderId);
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

    @Override
    @Transactional
    public GetOrderResponseDto shipOrder(Long orderId) {
        Order order = findOrderById(orderId);

        if(order.getOrderStatus() != OrderStatus.CREATED) {
            throw new InvalidOrderStateException("Only Orders with CREATED status can be shipped");
        }

        order.setOrderStatus(OrderStatus.SHIPPED);

        return orderMapper.toGetOrderResponseDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public GetOrderResponseDto deliverOrder(Long orderId) {
        Order order = findOrderById(orderId);


        if(order.getOrderStatus() != OrderStatus.SHIPPED) {
            throw new InvalidOrderStateException("Only Orders with SHIPPED status can be delivered");
        }

        for (OrderItem orderItem : order.getItemList()) {
            String operationId = "commit-" + orderId + "-" + orderItem.getProductId();
            inventoryServiceGateway.commitProduct(orderItem.getProductId(), orderItem.getQuantity(), operationId);
        }

        order.setOrderStatus(OrderStatus.DELIVERED);
        return orderMapper.toGetOrderResponseDto(orderRepository.save(order));
    }


    @Override
    @Transactional
    public GetOrderResponseDto cancelOrder(Long userId, Long orderId) {
        Order order = findOrderById(orderId);

        if (!order.getUserId().equals(userId)) {
            throw new OrderNotFoundException("Order not found with order_id : " + orderId);
        }

        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException("Orders with DELIVERED status cannot be cancelled");
        }

        if(order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("Order is already cancelled");
        }

        for (OrderItem orderItem : order.getItemList()) {
            String operationId = "release-" + orderId + "-" + orderItem.getProductId();
            inventoryServiceGateway.releaseProduct(orderItem.getProductId(), orderItem.getQuantity(), operationId);
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        return orderMapper.toGetOrderResponseDto(orderRepository.save(order));
    }

    private Order findOrderById(Long orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);

        if(optionalOrder.isEmpty()) {
            throw new OrderNotFoundException("Order not found with order_id : "  + orderId);
        }
        return optionalOrder.get();
    }

}
