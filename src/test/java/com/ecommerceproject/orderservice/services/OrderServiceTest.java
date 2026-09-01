package com.ecommerceproject.orderservice.services;

import com.ecommerceproject.orderservice.dtos.requestdto.CreateOrderRequestDto;
import com.ecommerceproject.orderservice.dtos.requestdto.OrderItemsRequestDto;
import com.ecommerceproject.orderservice.dtos.responsedto.CreateOrderResponseDto;
import com.ecommerceproject.orderservice.dtos.responsedto.GetOrderResponseDto;
import com.ecommerceproject.orderservice.dtos.responsedto.ProductResponseDto;
import com.ecommerceproject.orderservice.exceptions.InvalidOrderStateException;
import com.ecommerceproject.orderservice.exceptions.OrderNotFoundException;
import com.ecommerceproject.orderservice.gateways.InventoryServiceGateway;
import com.ecommerceproject.orderservice.gateways.ProductServiceGateway;
import com.ecommerceproject.orderservice.mappers.OrderMapper;
import com.ecommerceproject.orderservice.models.Order;
import com.ecommerceproject.orderservice.models.OrderItem;
import com.ecommerceproject.orderservice.models.enums.OrderStatus;
import com.ecommerceproject.orderservice.repositories.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ProductServiceGateway productServiceGateway;

    @Mock
    private InventoryServiceGateway inventoryServiceGateway;

    @InjectMocks
    private OrderService orderService;


/*
 * =========================================================
 * CREATE ORDER
 * =========================================================
 */

    @Test
    void createOrder_withValidItems_shouldCreateOrder() {

        CreateOrderRequestDto request = createRequest(createRequestItem(10L, 2));

        ProductResponseDto product = createProduct(10L, "Laptop", new BigDecimal("1000.00"));

        when(productServiceGateway.getProductById(10L))
                .thenReturn(product);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    order.setId(100L);
                    return order;
                });

        CreateOrderResponseDto expectedResponse = new CreateOrderResponseDto();

        expectedResponse.setOrderId(100L);
        expectedResponse.setOrderStatus(OrderStatus.CREATED);
        expectedResponse.setTotalAmount(new BigDecimal("2000.00"));

        when(orderMapper.toCreateOrderResponseDto(any(Order.class)))
                .thenReturn(expectedResponse);

        CreateOrderResponseDto response =
                orderService.createOrder(1L, request);

        assertEquals(100L, response.getOrderId());
        assertEquals(
                OrderStatus.CREATED,
                response.getOrderStatus()
        );
        assertEquals(
                new BigDecimal("2000.00"),
                response.getTotalAmount()
        );

        verify(productServiceGateway)
                .getProductById(10L);

        verify(inventoryServiceGateway)
                .reserveProduct(
                        eq(10L),
                        eq(2),
                        anyString()
                );

        verify(orderRepository)
                .save(any(Order.class));
    }

    @Test
    void createOrder_shouldSetUserIdAndCreatedStatus() {

        CreateOrderRequestDto request = createRequest(createRequestItem(10L, 1));

        when(productServiceGateway.getProductById(10L))
                .thenReturn(
                        createProduct(10L, "Laptop", new BigDecimal("1000.00"))
                );

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(orderMapper.toCreateOrderResponseDto(any(Order.class)))
                .thenReturn(new CreateOrderResponseDto());

        orderService.createOrder(55L, request);

        ArgumentCaptor<Order> orderCaptor =
                ArgumentCaptor.forClass(Order.class);

        verify(orderRepository)
                .save(orderCaptor.capture());

        Order savedOrder =
                orderCaptor.getValue();

        assertEquals(
                55L,
                savedOrder.getUserId()
        );

        assertEquals(
                OrderStatus.CREATED,
                savedOrder.getOrderStatus()
        );
    }

    @Test
    void createOrder_shouldCalculateTotalAmount() {

        CreateOrderRequestDto request = createRequest(createRequestItem(10L, 2), createRequestItem(20L, 3));

        when(productServiceGateway.getProductById(10L))
                .thenReturn(
                        createProduct(10L, "Laptop", new BigDecimal("100.00")));

        when(productServiceGateway.getProductById(20L))
                .thenReturn(createProduct(20L, "Mouse", new BigDecimal("50.00")));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        when(orderMapper.toCreateOrderResponseDto(any(Order.class)))
                .thenReturn(new CreateOrderResponseDto());

        orderService.createOrder(1L, request);

        ArgumentCaptor<Order> orderCaptor =
                ArgumentCaptor.forClass(Order.class);

        verify(orderRepository)
                .save(orderCaptor.capture());

        Order order = orderCaptor.getValue();

        /*
         * Laptop: 100 x 2 = 200
         * Mouse :  50 x 3 = 150
         *
         * Total = 350
         */
        assertEquals(
                new BigDecimal("350.00"),
                order.getTotalAmount()
        );
    }

    @Test
    void createOrder_shouldCreateOrderItemsFromProductDetails() {

        CreateOrderRequestDto request =
                createRequest(
                        createRequestItem(10L, 2)
                );

        when(productServiceGateway.getProductById(10L))
                .thenReturn(
                        createProduct(
                                10L,
                                "Gaming Laptop",
                                new BigDecimal("1200.00")
                        )
                );

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        when(orderMapper.toCreateOrderResponseDto(any(Order.class)))
                .thenReturn(new CreateOrderResponseDto());

        orderService.createOrder(1L, request);

        ArgumentCaptor<Order> captor =
                ArgumentCaptor.forClass(Order.class);

        verify(orderRepository)
                .save(captor.capture());

        Order savedOrder =
                captor.getValue();

        assertEquals(
                1,
                savedOrder.getItemList().size()
        );

        OrderItem item =
                savedOrder
                        .getItemList()
                        .get(0);

        assertEquals(
                10L,
                item.getProductId()
        );

        assertEquals(
                "Gaming Laptop",
                item.getName()
        );

        assertEquals(
                new BigDecimal("1200.00"),
                item.getPrice()
        );

        assertEquals(
                2,
                item.getQuantity()
        );

        /*
         * Verifies your bidirectional helper:
         *
         * order.addOrderItem(orderItem)
         */
        assertSame(
                savedOrder,
                item.getOrder()
        );
    }


/*
 * =========================================================
 * DUPLICATE PRODUCT CONSOLIDATION
 * =========================================================
 */

    @Test
    void createOrder_withDuplicateProducts_shouldConsolidateQuantities() {

        CreateOrderRequestDto request =
                createRequest(
                        createRequestItem(10L, 2),
                        createRequestItem(10L, 3)
                );

        when(productServiceGateway.getProductById(10L))
                .thenReturn(
                        createProduct(
                                10L,
                                "Laptop",
                                new BigDecimal("100.00")
                        )
                );

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        when(orderMapper.toCreateOrderResponseDto(any(Order.class)))
                .thenReturn(new CreateOrderResponseDto());

        orderService.createOrder(1L, request);

        /*
         * Product service should only be called once after
         * consolidation.
         */
        verify(productServiceGateway, times(1))
                .getProductById(10L);

        /*
         * 2 + 3 = 5
         */
        verify(inventoryServiceGateway)
                .reserveProduct(
                        eq(10L),
                        eq(5),
                        anyString()
                );

        ArgumentCaptor<Order> captor =
                ArgumentCaptor.forClass(Order.class);

        verify(orderRepository)
                .save(captor.capture());

        Order savedOrder =
                captor.getValue();

        assertEquals(
                1,
                savedOrder.getItemList().size()
        );

        assertEquals(
                5,
                savedOrder
                        .getItemList()
                        .get(0)
                        .getQuantity()
        );

        assertEquals(
                new BigDecimal("500.00"),
                savedOrder.getTotalAmount()
        );
    }

    @Test
    void createOrder_withDifferentProducts_shouldReserveEachProduct() {

        CreateOrderRequestDto request =
                createRequest(
                        createRequestItem(10L, 2),
                        createRequestItem(20L, 4)
                );

        when(productServiceGateway.getProductById(10L))
                .thenReturn(
                        createProduct(
                                10L,
                                "Laptop",
                                new BigDecimal("100.00")
                        )
                );

        when(productServiceGateway.getProductById(20L))
                .thenReturn(
                        createProduct(
                                20L,
                                "Mouse",
                                new BigDecimal("25.00")
                        )
                );

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        when(orderMapper.toCreateOrderResponseDto(any(Order.class)))
                .thenReturn(new CreateOrderResponseDto());

        orderService.createOrder(1L, request);

        verify(inventoryServiceGateway)
                .reserveProduct(
                        eq(10L),
                        eq(2),
                        anyString()
                );

        verify(inventoryServiceGateway)
                .reserveProduct(
                        eq(20L),
                        eq(4),
                        anyString()
                );
    }


/*
 * =========================================================
 * CREATE ORDER - COMPENSATION
 * =========================================================
 */

//    @Test
//    void createOrder_whenSecondReservationFails_shouldReleaseFirstReservation() {
//
//        CreateOrderRequestDto request =
//                createRequest(
//                        createRequestItem(10L, 2),
//                        createRequestItem(20L, 3)
//                );
//
//        when(productServiceGateway.getProductById(10L))
//                .thenReturn(
//                        createProduct(
//                                10L,
//                                "Laptop",
//                                new BigDecimal("100.00")
//                        )
//                );
//
//        when(productServiceGateway.getProductById(20L))
//                .thenReturn(
//                        createProduct(
//                                20L,
//                                "Mouse",
//                                new BigDecimal("50.00")
//                        )
//                );
//
//        RuntimeException failure = new RuntimeException("Reservation failed");
//
//        when(inventoryServiceGateway.reserveProduct(
//                eq(20L),
//                eq(3),
//                anyString()
//        )).thenThrow(failure);
//
//        RuntimeException thrown =
//                assertThrows(RuntimeException.class,
//                        () -> orderService.createOrder(1L, request)
//                );
//
//        assertSame(
//                failure,
//                thrown
//        );
//
//        /*
//         * Product 10 was reserved successfully,
//         * therefore it must be compensated.
//         */
//        verify(inventoryServiceGateway)
//                .releaseProduct(
//                        eq(10L),
//                        eq(2),
//                        anyString()
//                );
//
//        /*
//         * Product 20 reservation failed, so it must NOT be released.
//         */
//        verify(inventoryServiceGateway, never())
//                .releaseProduct(
//                        eq(20L),
//                        any(),
//                        anyString()
//                );
//
//        verify(orderRepository, never())
//                .save(any());
//    }

    @Test
    void createOrder_whenSecondProductLookupFails_shouldReleasePreviousReservation() {

        CreateOrderRequestDto request =
                createRequest(
                        createRequestItem(10L, 2),
                        createRequestItem(20L, 3)
                );

        when(productServiceGateway.getProductById(10L))
                .thenReturn(
                        createProduct(
                                10L,
                                "Laptop",
                                new BigDecimal("100.00")
                        )
                );

        RuntimeException failure =
                new RuntimeException(
                        "Product service failed"
                );

        when(productServiceGateway.getProductById(20L))
                .thenThrow(failure);

        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () -> orderService.createOrder(
                                1L,
                                request
                        )
                );

        assertSame(
                failure,
                thrown
        );

        verify(inventoryServiceGateway)
                .releaseProduct(
                        eq(10L),
                        eq(2),
                        anyString()
                );

        verify(orderRepository, never())
                .save(any());
    }

    @Test
    void createOrder_whenRepositorySaveFails_shouldReleaseAllReservations() {

        CreateOrderRequestDto request =
                createRequest(
                        createRequestItem(10L, 2),
                        createRequestItem(20L, 3)
                );

        when(productServiceGateway.getProductById(10L))
                .thenReturn(
                        createProduct(
                                10L,
                                "Laptop",
                                new BigDecimal("100.00")
                        )
                );

        when(productServiceGateway.getProductById(20L))
                .thenReturn(
                        createProduct(
                                20L,
                                "Mouse",
                                new BigDecimal("50.00")
                        )
                );

        RuntimeException databaseFailure =
                new RuntimeException(
                        "Database failure"
                );

        when(orderRepository.save(any(Order.class)))
                .thenThrow(databaseFailure);

        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () -> orderService.createOrder(
                                1L,
                                request
                        )
                );

        assertSame(
                databaseFailure,
                thrown
        );

        verify(inventoryServiceGateway)
                .releaseProduct(
                        eq(10L),
                        eq(2),
                        anyString()
                );

        verify(inventoryServiceGateway)
                .releaseProduct(
                        eq(20L),
                        eq(3),
                        anyString()
                );
    }

//    @Test
//    void createOrder_whenCompensationFails_shouldStillThrowOriginalException() {
//
//        CreateOrderRequestDto request =
//                createRequest(
//                        createRequestItem(10L, 2),
//                        createRequestItem(20L, 3)
//                );
//
//        when(productServiceGateway.getProductById(10L))
//                .thenReturn(
//                        createProduct(
//                                10L,
//                                "Laptop",
//                                new BigDecimal("100.00")
//                        )
//                );
//
//        when(productServiceGateway.getProductById(20L))
//                .thenReturn(
//                        createProduct(
//                                20L,
//                                "Mouse",
//                                new BigDecimal("50.00")
//                        )
//                );
//
//        RuntimeException originalFailure =
//                new RuntimeException(
//                        "Second reservation failed"
//                );
//
//        when(inventoryServiceGateway.reserveProduct(
//                eq(20L),
//                eq(3),
//                anyString()
//        )).thenThrow(originalFailure);
//
//        doThrow(
//                new RuntimeException(
//                        "Compensation failed"
//                )
//        ).when(inventoryServiceGateway)
//                .releaseProduct(
//                        eq(10L),
//                        eq(2),
//                        anyString()
//                );
//
//        RuntimeException thrown =
//                assertThrows(
//                        RuntimeException.class,
//                        () -> orderService.createOrder(
//                                1L,
//                                request
//                        )
//                );
//
//        /*
//         * Your service intentionally logs compensation failure
//         * and rethrows the ORIGINAL operation failure.
//         */
//        assertSame(
//                originalFailure,
//                thrown
//        );
//    }


/*
 * =========================================================
 * GET ORDER BY ID
 * =========================================================
 */

    @Test
    void getOrderById_whenOrderExistsAndBelongsToUser_shouldReturnOrder() {

        Order order =
                createOrder(
                        100L,
                        1L,
                        OrderStatus.CREATED
                );

        GetOrderResponseDto expected =
                new GetOrderResponseDto();

        expected.setOrderId(100L);

        when(orderRepository.findById(100L))
                .thenReturn(
                        Optional.of(order)
                );

        when(orderMapper.toGetOrderResponseDto(order))
                .thenReturn(expected);

        GetOrderResponseDto response =
                orderService.getOrderById(
                        1L,
                        100L
                );

        assertSame(
                expected,
                response
        );

        verify(orderMapper)
                .toGetOrderResponseDto(order);
    }

    @Test
    void getOrderById_whenOrderDoesNotExist_shouldThrowOrderNotFoundException() {

        when(orderRepository.findById(100L))
                .thenReturn(
                        Optional.empty()
                );

        OrderNotFoundException exception =
                assertThrows(
                        OrderNotFoundException.class,
                        () -> orderService.getOrderById(
                                1L,
                                100L
                        )
                );

        assertEquals(
                "Order not found with order_id : 100",
                exception.getMessage()
        );

        verify(orderMapper, never())
                .toGetOrderResponseDto(any());
    }

    @Test
    void getOrderById_whenOrderBelongsToDifferentUser_shouldThrowOrderNotFoundException() {

        Order order =
                createOrder(
                        100L,
                        99L,
                        OrderStatus.CREATED
                );

        when(orderRepository.findById(100L))
                .thenReturn(
                        Optional.of(order)
                );

        OrderNotFoundException exception =
                assertThrows(
                        OrderNotFoundException.class,
                        () -> orderService.getOrderById(
                                1L,
                                100L
                        )
                );

        assertEquals(
                "Order not found with order_id : 100",
                exception.getMessage()
        );

        verify(orderMapper, never())
                .toGetOrderResponseDto(any());
    }


/*
 * =========================================================
 * GET ALL ORDERS
 * =========================================================
 */

    @Test
    void getAllOrders_shouldReturnMappedPageForUser() {

        Pageable pageable =
                PageRequest.of(0, 10);

        Order first =
                createOrder(
                        100L,
                        1L,
                        OrderStatus.CREATED
                );

        Order second =
                createOrder(
                        101L,
                        1L,
                        OrderStatus.SHIPPED
                );

        Page<Order> orderPage =
                new PageImpl<>(
                        List.of(first, second),
                        pageable,
                        2
                );

        GetOrderResponseDto firstDto =
                new GetOrderResponseDto();

        firstDto.setOrderId(100L);

        GetOrderResponseDto secondDto =
                new GetOrderResponseDto();

        secondDto.setOrderId(101L);

        when(orderRepository.findByUserId(
                1L,
                pageable
        )).thenReturn(orderPage);

        when(orderMapper.toGetOrderResponseDto(first))
                .thenReturn(firstDto);

        when(orderMapper.toGetOrderResponseDto(second))
                .thenReturn(secondDto);

        Page<GetOrderResponseDto> result =
                orderService.getAllOrders(
                        1L,
                        pageable
                );

        assertEquals(
                2,
                result.getTotalElements()
        );

        assertEquals(
                100L,
                result.getContent()
                        .get(0)
                        .getOrderId()
        );

        assertEquals(
                101L,
                result.getContent()
                        .get(1)
                        .getOrderId()
        );

        verify(orderRepository)
                .findByUserId(
                        1L,
                        pageable
                );
    }

    @Test
    void getAllOrders_whenNoOrders_shouldReturnEmptyPage() {

        Pageable pageable =
                PageRequest.of(0, 10);

        when(orderRepository.findByUserId(
                1L,
                pageable
        )).thenReturn(
                Page.empty(pageable)
        );

        Page<GetOrderResponseDto> result =
                orderService.getAllOrders(
                        1L,
                        pageable
                );

        assertTrue(
                result.isEmpty()
        );

        verify(orderMapper, never())
                .toGetOrderResponseDto(any());
    }


/*
 * =========================================================
 * FIND ORDER BY USER ID
 * =========================================================
 */

    @Test
    void findOrderByUserId_whenOrderExists_shouldReturnOrder() {

        Order order =
                createOrder(
                        100L,
                        1L,
                        OrderStatus.CREATED
                );

        when(orderRepository.findByUserId(1L))
                .thenReturn(
                        Optional.of(order)
                );

        Order result =
                orderService.findOrderByUserId(1L);

        assertSame(
                order,
                result
        );
    }

    @Test
    void findOrderByUserId_whenOrderDoesNotExist_shouldThrowOrderNotFoundException() {

        when(orderRepository.findByUserId(1L))
                .thenReturn(
                        Optional.empty()
                );

        OrderNotFoundException exception =
                assertThrows(
                        OrderNotFoundException.class,
                        () -> orderService.findOrderByUserId(1L)
                );

        assertEquals(
                "Order not found with user_id : 1",
                exception.getMessage()
        );
    }


/*
 * =========================================================
 * SHIP ORDER
 * =========================================================
 */

    @Test
    void shipOrder_whenCreated_shouldChangeStatusToShipped() {

        Order order =
                createOrder(
                        100L,
                        1L,
                        OrderStatus.CREATED
                );

        GetOrderResponseDto response =
                new GetOrderResponseDto();

        response.setOrderId(100L);
        response.setOrderStatus(
                OrderStatus.SHIPPED
        );

        when(orderRepository.findById(100L))
                .thenReturn(
                        Optional.of(order)
                );

        when(orderRepository.save(order))
                .thenReturn(order);

        when(orderMapper.toGetOrderResponseDto(order))
                .thenReturn(response);

        GetOrderResponseDto result =
                orderService.shipOrder(100L);

        assertEquals(
                OrderStatus.SHIPPED,
                order.getOrderStatus()
        );

        assertSame(
                response,
                result
        );

        verify(orderRepository)
                .save(order);
    }

    @Test
    void shipOrder_whenAlreadyShipped_shouldThrowInvalidOrderStateException() {

        Order order =
                createOrder(
                        100L,
                        1L,
                        OrderStatus.SHIPPED
                );

        when(orderRepository.findById(100L))
                .thenReturn(
                        Optional.of(order)
                );

        InvalidOrderStateException exception =
                assertThrows(
                        InvalidOrderStateException.class,
                        () -> orderService.shipOrder(100L)
                );

        assertEquals(
                "Only Orders with CREATED status can be shipped",
                exception.getMessage()
        );

        verify(orderRepository, never())
                .save(any());
    }

    @Test
    void shipOrder_whenDelivered_shouldThrowInvalidOrderStateException() {

        Order order =
                createOrder(
                        100L,
                        1L,
                        OrderStatus.DELIVERED
                );

        when(orderRepository.findById(100L))
                .thenReturn(
                        Optional.of(order)
                );

        assertThrows(
                InvalidOrderStateException.class,
                () -> orderService.shipOrder(100L)
        );

        verify(orderRepository, never())
                .save(any());
    }

    @Test
    void shipOrder_whenCancelled_shouldThrowInvalidOrderStateException() {

        Order order =
                createOrder(
                        100L,
                        1L,
                        OrderStatus.CANCELLED
                );

        when(orderRepository.findById(100L))
                .thenReturn(
                        Optional.of(order)
                );

        assertThrows(
                InvalidOrderStateException.class,
                () -> orderService.shipOrder(100L)
        );

        verify(orderRepository, never())
                .save(any());
    }

    @Test
    void shipOrder_whenOrderDoesNotExist_shouldThrowOrderNotFoundException() {

        when(orderRepository.findById(100L))
                .thenReturn(
                        Optional.empty()
                );

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.shipOrder(100L)
        );

        verify(orderRepository, never())
                .save(any());
    }


/*
 * =========================================================
 * DELIVER ORDER
 * =========================================================
 */

    @Test
    void deliverOrder_whenShipped_shouldCommitInventoryAndDeliverOrder() {

        Order order =
                createOrder(
                        100L,
                        1L,
                        OrderStatus.SHIPPED
                );

        order.addOrderItem(
                createOrderItem(
                        10L,
                        "Laptop",
                        new BigDecimal("100.00"),
                        2
                )
        );

        order.addOrderItem(
                createOrderItem(
                        20L,
                        "Mouse",
                        new BigDecimal("50.00"),
                        3
                )
        );

        GetOrderResponseDto response =
                new GetOrderResponseDto();

        response.setOrderId(100L);
        response.setOrderStatus(
                OrderStatus.DELIVERED
        );

        when(orderRepository.findById(100L))
                .thenReturn(
                        Optional.of(order)
                );

        when(orderRepository.save(order))
                .thenReturn(order);

        when(orderMapper.toGetOrderResponseDto(order))
                .thenReturn(response);

        GetOrderResponseDto result =
                orderService.deliverOrder(100L);

        verify(inventoryServiceGateway)
                .commitProduct(
                        eq(10L),
                        eq(2),
                        eq("commit-100-10")
                );

        verify(inventoryServiceGateway)
                .commitProduct(
                        eq(20L),
                        eq(3),
                        eq("commit-100-20")
                );

        assertEquals(
                OrderStatus.DELIVERED,
                order.getOrderStatus()
        );

        assertSame(
                response,
                result
        );
    }

    @Test
    void deliverOrder_whenCreated_shouldThrowInvalidOrderStateException() {

        Order order =
                createOrder(
                        100L,
                        1L,
                        OrderStatus.CREATED
                );

        when(orderRepository.findById(100L))
                .thenReturn(
                        Optional.of(order)
                );

        InvalidOrderStateException exception =
                assertThrows(
                        InvalidOrderStateException.class,
                        () -> orderService.deliverOrder(100L)
                );

        assertEquals(
                "Only Orders with SHIPPED status can be delivered",
                exception.getMessage()
        );

        verify(inventoryServiceGateway, never())
                .commitProduct(
                        any(),
                        any(),
                        anyString()
                );

        verify(orderRepository, never())
                .save(any());
    }

    @Test
    void deliverOrder_whenCancelled_shouldThrowInvalidOrderStateException() {

        Order order =
                createOrder(
                        100L,
                        1L,
                        OrderStatus.CANCELLED
                );

        when(orderRepository.findById(100L))
                .thenReturn(
                        Optional.of(order)
                );

        assertThrows(
                InvalidOrderStateException.class,
                () -> orderService.deliverOrder(100L)
        );

        verify(inventoryServiceGateway, never())
                .commitProduct(
                        any(),
                        any(),
                        anyString()
                );
    }

    @Test
    void deliverOrder_whenDelivered_shouldThrowInvalidOrderStateException() {

        Order order =
                createOrder(
                        100L,
                        1L,
                        OrderStatus.DELIVERED
                );

        when(orderRepository.findById(100L))
                .thenReturn(
                        Optional.of(order)
                );

        assertThrows(
                InvalidOrderStateException.class,
                () -> orderService.deliverOrder(100L)
        );

        verify(inventoryServiceGateway, never())
                .commitProduct(
                        any(),
                        any(),
                        anyString()
                );
    }

    @Test
    void deliverOrder_whenInventoryCommitFails_shouldNotMarkOrderDelivered() {

        Order order =
                createOrder(
                        100L,
                        1L,
                        OrderStatus.SHIPPED
                );

        order.addOrderItem(
                createOrderItem(
                        10L,
                        "Laptop",
                        new BigDecimal("100.00"),
                        2
                )
        );

        when(orderRepository.findById(100L))
                .thenReturn(
                        Optional.of(order)
                );

        RuntimeException failure =
                new RuntimeException(
                        "Inventory commit failed"
                );

        when(inventoryServiceGateway.commitProduct(
                eq(10L),
                eq(2),
                eq("commit-100-10")
        )).thenThrow(failure);

        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () -> orderService.deliverOrder(100L)
                );

        assertSame(
                failure,
                thrown
        );

        /*
         * Status assignment occurs only after all commits.
         */
        assertEquals(
                OrderStatus.SHIPPED,
                order.getOrderStatus()
        );

        verify(orderRepository, never())
                .save(any());
    }


/*
 * =========================================================
 * CANCEL ORDER
 * =========================================================
 */

    @Test
    void cancelOrder_whenCreated_shouldReleaseInventoryAndCancelOrder() {

        Order order =
                createOrder(
                        100L,
                        1L,
                        OrderStatus.CREATED
                );

        order.addOrderItem(
                createOrderItem(
                        10L,
                        "Laptop",
                        new BigDecimal("100.00"),
                        2
                )
        );

        when(orderRepository.findById(100L))
                .thenReturn(
                        Optional.of(order)
                );

        when(orderRepository.save(order))
                .thenReturn(order);

        GetOrderResponseDto response =
                new GetOrderResponseDto();

        response.setOrderStatus(
                OrderStatus.CANCELLED
        );

        when(orderMapper.toGetOrderResponseDto(order))
                .thenReturn(response);

        GetOrderResponseDto result =
                orderService.cancelOrder(
                        1L,
                        100L
                );

        verify(inventoryServiceGateway)
                .releaseProduct(
                        eq(10L),
                        eq(2),
                        eq("release-100-10")
                );

        assertEquals(
                OrderStatus.CANCELLED,
                order.getOrderStatus()
        );

        assertSame(
                response,
                result
        );
    }

    @Test
    void cancelOrder_whenShipped_shouldReleaseInventoryAndCancelOrder() {

        Order order =
                createOrder(
                        100L,
                        1L,
                        OrderStatus.SHIPPED
                );

        order.addOrderItem(
                createOrderItem(
                        10L,
                        "Laptop",
                        new BigDecimal("100.00"),
                        2
                )
        );

        when(orderRepository.findById(100L))
                .thenReturn(
                        Optional.of(order)
                );

        when(orderRepository.save(order))
                .thenReturn(order);

        when(orderMapper.toGetOrderResponseDto(order))
                .thenReturn(new GetOrderResponseDto());

        orderService.cancelOrder(
                1L,
                100L
        );

        verify(inventoryServiceGateway)
                .releaseProduct(
                        eq(10L),
                        eq(2),
                        eq("release-100-10")
                );

        assertEquals(
                OrderStatus.CANCELLED,
                order.getOrderStatus()
        );
    }

    @Test
    void cancelOrder_whenDifferentUser_shouldThrowOrderNotFoundException() {

        Order order =
                createOrder(
                        100L,
                        99L,
                        OrderStatus.CREATED
                );

        when(orderRepository.findById(100L))
                .thenReturn(
                        Optional.of(order)
                );

        OrderNotFoundException exception =
                assertThrows(
                        OrderNotFoundException.class,
                        () -> orderService.cancelOrder(
                                1L,
                                100L
                        )
                );

        assertEquals(
                "Order not found with order_id : 100",
                exception.getMessage()
        );

        verify(inventoryServiceGateway, never())
                .releaseProduct(
                        any(),
                        any(),
                        anyString()
                );

        verify(orderRepository, never())
                .save(any());
    }

    @Test
    void cancelOrder_whenDelivered_shouldThrowInvalidOrderStateException() {

        Order order =
                createOrder(
                        100L,
                        1L,
                        OrderStatus.DELIVERED
                );

        when(orderRepository.findById(100L))
                .thenReturn(
                        Optional.of(order)
                );

        InvalidOrderStateException exception =
                assertThrows(
                        InvalidOrderStateException.class,
                        () -> orderService.cancelOrder(
                                1L,
                                100L
                        )
                );

        assertEquals(
                "Orders with DELIVERED status cannot be cancelled",
                exception.getMessage()
        );

        verify(inventoryServiceGateway, never())
                .releaseProduct(
                        any(),
                        any(),
                        anyString()
                );

        verify(orderRepository, never())
                .save(any());
    }

    @Test
    void cancelOrder_whenAlreadyCancelled_shouldThrowInvalidOrderStateException() {

        Order order =
                createOrder(
                        100L,
                        1L,
                        OrderStatus.CANCELLED
                );

        when(orderRepository.findById(100L))
                .thenReturn(
                        Optional.of(order)
                );

        InvalidOrderStateException exception =
                assertThrows(
                        InvalidOrderStateException.class,
                        () -> orderService.cancelOrder(
                                1L,
                                100L
                        )
                );

        assertEquals(
                "Order is already cancelled",
                exception.getMessage()
        );

        verify(inventoryServiceGateway, never())
                .releaseProduct(
                        any(),
                        any(),
                        anyString()
                );

        verify(orderRepository, never())
                .save(any());
    }

    @Test
    void cancelOrder_whenOrderDoesNotExist_shouldThrowOrderNotFoundException() {

        when(orderRepository.findById(100L))
                .thenReturn(
                        Optional.empty()
                );

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.cancelOrder(
                        1L,
                        100L
                )
        );

        verify(inventoryServiceGateway, never())
                .releaseProduct(
                        any(),
                        any(),
                        anyString()
                );
    }

    @Test
    void cancelOrder_whenInventoryReleaseFails_shouldNotMarkOrderCancelled() {

        Order order =
                createOrder(
                        100L,
                        1L,
                        OrderStatus.CREATED
                );

        order.addOrderItem(
                createOrderItem(
                        10L,
                        "Laptop",
                        new BigDecimal("100.00"),
                        2
                )
        );

        when(orderRepository.findById(100L))
                .thenReturn(
                        Optional.of(order)
                );

        RuntimeException failure =
                new RuntimeException(
                        "Inventory release failed"
                );

        when(inventoryServiceGateway.releaseProduct(
                eq(10L),
                eq(2),
                eq("release-100-10")
        )).thenThrow(failure);

        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class,
                        () -> orderService.cancelOrder(
                                1L,
                                100L
                        )
                );

        assertSame(
                failure,
                thrown
        );

        assertEquals(
                OrderStatus.CREATED,
                order.getOrderStatus()
        );

        verify(orderRepository, never())
                .save(any());
    }


/*
 * =========================================================
 * HELPER METHODS
 * =========================================================
 */

    private CreateOrderRequestDto createRequest(
            OrderItemsRequestDto... items) {

        CreateOrderRequestDto request =
                new CreateOrderRequestDto();

        request.setOrderItems(
                List.of(items)
        );

        return request;
    }

    private OrderItemsRequestDto createRequestItem(
            Long productId,
            Integer quantity) {

        OrderItemsRequestDto item =
                new OrderItemsRequestDto();

        item.setProductId(productId);
        item.setQuantity(quantity);

        return item;
    }

    private ProductResponseDto createProduct(
            Long id,
            String title,
            BigDecimal price) {

        ProductResponseDto product =
                new ProductResponseDto();

        product.setId(id);
        product.setTitle(title);
        product.setPrice(price);
        product.setQty(100);

        return product;
    }

    private Order createOrder(
            Long orderId,
            Long userId,
            OrderStatus status) {

        Order order =
                new Order();

        order.setId(orderId);
        order.setUserId(userId);
        order.setOrderStatus(status);
        order.setTotalAmount(
                new BigDecimal("100.00")
        );

        return order;
    }

    private OrderItem createOrderItem(
            Long productId,
            String name,
            BigDecimal price,
            Integer quantity) {

        OrderItem item =
                new OrderItem();

        item.setProductId(productId);
        item.setName(name);
        item.setPrice(price);
        item.setQuantity(quantity);

        return item;
    }
}