package com.ecommerceproject.orderservice.controllers;

import com.ecommerceproject.orderservice.controlleradvice.GlobalExceptionHandler;
import com.ecommerceproject.orderservice.dtos.requestdto.CreateOrderRequestDto;
import com.ecommerceproject.orderservice.dtos.requestdto.OrderItemsRequestDto;
import com.ecommerceproject.orderservice.dtos.responsedto.CreateOrderResponseDto;
import com.ecommerceproject.orderservice.dtos.responsedto.GetOrderResponseDto;
import com.ecommerceproject.orderservice.models.enums.OrderStatus;
import com.ecommerceproject.orderservice.services.IOrderService;
import com.ecommerceproject.orderservice.services.ITokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = OrderController.class,
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.security.autoconfigure.servlet.SecurityAutoConfiguration"
        }
)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IOrderService orderService;

    @MockitoBean
    private ITokenService tokenService;
/*
 * ---------------------------------------------------------
 * CREATE ORDER
 * ---------------------------------------------------------
 */

    @Test
    void createOrder_withValidRequest_shouldReturn201() throws Exception {

        setAuthenticatedUser(1L);

        CreateOrderRequestDto request = createValidRequest(10L, 2);

        CreateOrderResponseDto response = new CreateOrderResponseDto();
        response.setOrderId(100L);
        response.setTotalAmount(new BigDecimal("200.00"));
        response.setOrderStatus(OrderStatus.CREATED);

        when(orderService.createOrder(eq(1L), any(CreateOrderRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/orders")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(100))
                .andExpect(jsonPath("$.totalAmount").value(200.00))
                .andExpect(jsonPath("$.orderStatus").value("CREATED"));

        verify(orderService)
                .createOrder(eq(1L), any(CreateOrderRequestDto.class));
    }


/*
 * ---------------------------------------------------------
 * CREATE ORDER - ORDER ITEMS VALIDATION
 * ---------------------------------------------------------
 */

    @Test
    void createOrder_withNullOrderItems_shouldReturn400() throws Exception {

        setAuthenticatedUser(1L);

        CreateOrderRequestDto request = new CreateOrderRequestDto();
        request.setOrderItems(null);

        mockMvc.perform(
                        post("/orders")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.orderItems").value("order must contain at least one item"));

        verify(orderService, never())
                .createOrder(any(), any());
    }

    @Test
    void createOrder_withEmptyOrderItems_shouldReturn400() throws Exception {

        setAuthenticatedUser(1L);

        CreateOrderRequestDto request = new CreateOrderRequestDto();
        request.setOrderItems(Collections.emptyList());

        mockMvc.perform(
                        post("/orders")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.orderItems").value("order must contain at least one item"));

        verify(orderService, never())
                .createOrder(any(), any());
    }


/*
 * ---------------------------------------------------------
 * PRODUCT ID VALIDATION
 * ---------------------------------------------------------
 */

    @Test
    void createOrder_withNullProductId_shouldReturn400() throws Exception {

        setAuthenticatedUser(1L);

        CreateOrderRequestDto request =
                createValidRequest(null, 2);

        mockMvc.perform(
                        post("/orders")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors['orderItems[0].productId']").value("product id cannot be null"));

        verify(orderService, never())
                .createOrder(any(), any());
    }

    @Test
    void createOrder_withZeroProductId_shouldReturn400() throws Exception {

        setAuthenticatedUser(1L);

        CreateOrderRequestDto request = createValidRequest(0L, 2);

        mockMvc.perform(
                        post("/orders")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors['orderItems[0].productId']").value("product id cannot be negative"));

        verify(orderService, never())
                .createOrder(any(), any());
    }

    @Test
    void createOrder_withNegativeProductId_shouldReturn400() throws Exception {

        setAuthenticatedUser(1L);

        CreateOrderRequestDto request = createValidRequest(-10L, 2);

        mockMvc.perform(
                        post("/orders")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors['orderItems[0].productId']") .value("product id cannot be negative"));

        verify(orderService, never())
                .createOrder(any(), any());
    }


/*
 * ---------------------------------------------------------
 * QUANTITY VALIDATION
 * ---------------------------------------------------------
 */

    @Test
    void createOrder_withNullQuantity_shouldReturn400() throws Exception {

        setAuthenticatedUser(1L);

        CreateOrderRequestDto request = createValidRequest(10L, null);

        mockMvc.perform(
                        post("/orders")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors['orderItems[0].quantity']").value("quantity cannot be null"));

        verify(orderService, never())
                .createOrder(any(), any());
    }

    @Test
    void createOrder_withZeroQuantity_shouldReturn400() throws Exception {

        setAuthenticatedUser(1L);

        CreateOrderRequestDto request =
                createValidRequest(10L, 0);

        mockMvc.perform(
                        post("/orders")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors['orderItems[0].quantity']").value("quantity should be greater than 0"));

        verify(orderService, never())
                .createOrder(any(), any());
    }

    @Test
    void createOrder_withNegativeQuantity_shouldReturn400() throws Exception {

        setAuthenticatedUser(1L);

        CreateOrderRequestDto request =
                createValidRequest(10L, -5);

        mockMvc.perform(
                        post("/orders")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors['orderItems[0].quantity']").value("quantity should be greater than 0"));

        verify(orderService, never())
                .createOrder(any(), any());
    }


/*
 * ---------------------------------------------------------
 * GET ORDER
 * ---------------------------------------------------------
 */

    @Test
    void getOrderById_withValidOrderId_shouldReturn200() throws Exception {

        setAuthenticatedUser(1L);

        GetOrderResponseDto response = createOrderResponse();

        when(orderService.getOrderById(1L, 100L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/orders/100")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(100))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.totalAmount").value(200.00))
                .andExpect(jsonPath("$.orderStatus").value("CREATED"));

        verify(orderService)
                .getOrderById(1L, 100L);
    }


/*
 * ---------------------------------------------------------
 * GET MY ORDERS
 * ---------------------------------------------------------
 */

    @Test
    void getMyOrders_shouldReturn200() throws Exception {

        setAuthenticatedUser(1L);

        GetOrderResponseDto response = createOrderResponse();

        PageImpl<GetOrderResponseDto> page =
                new PageImpl<>(
                        List.of(response),
                        PageRequest.of(0, 10),
                        1
                );

        when(orderService.getAllOrders(eq(1L), any()))
                .thenReturn(page);

        mockMvc.perform(
                        get("/orders/my-orders")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderId").value(100))
                .andExpect(jsonPath("$.content[0].userId").value(1))
                .andExpect(jsonPath("$.content[0].orderStatus").value("CREATED"));

        verify(orderService)
                .getAllOrders(eq(1L), any());
    }


/*
 * ---------------------------------------------------------
 * SHIP ORDER
 * ---------------------------------------------------------
 */

    @Test
    void shipOrder_shouldReturn200() throws Exception {

        GetOrderResponseDto response = createOrderResponse();
        response.setOrderStatus(OrderStatus.SHIPPED);

        when(orderService.shipOrder(100L))
                .thenReturn(response);

        mockMvc.perform(
                        post("/orders/100/ship")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(100))
                .andExpect(jsonPath("$.orderStatus").value("SHIPPED"));

        verify(orderService)
                .shipOrder(100L);
    }


/*
 * ---------------------------------------------------------
 * DELIVER ORDER
 * ---------------------------------------------------------
 */

    @Test
    void deliverOrder_shouldReturn200() throws Exception {

        GetOrderResponseDto response = createOrderResponse();
        response.setOrderStatus(OrderStatus.DELIVERED);

        when(orderService.deliverOrder(100L))
                .thenReturn(response);

        mockMvc.perform(
                        post("/orders/100/deliver")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(100))
                .andExpect(jsonPath("$.orderStatus").value("DELIVERED"));

        verify(orderService)
                .deliverOrder(100L);
    }


/*
 * ---------------------------------------------------------
 * CANCEL ORDER
 * ---------------------------------------------------------
 */

    @Test
    void cancelOrder_shouldReturn200() throws Exception {

        setAuthenticatedUser(1L);

        GetOrderResponseDto response = createOrderResponse();
        response.setOrderStatus(OrderStatus.CANCELLED);

        when(orderService.cancelOrder(1L, 100L))
                .thenReturn(response);

        mockMvc.perform(
                        post("/orders/100/cancel")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(100))
                .andExpect(jsonPath("$.orderStatus").value("CANCELLED"));

        verify(orderService)
                .cancelOrder(1L, 100L);
    }


/*
 * ---------------------------------------------------------
 * HELPER METHODS
 * ---------------------------------------------------------
 */

    private CreateOrderRequestDto createValidRequest(
            Long productId,
            Integer quantity) {

        OrderItemsRequestDto item = new OrderItemsRequestDto();

        item.setProductId(productId);
        item.setQuantity(quantity);

        CreateOrderRequestDto request = new CreateOrderRequestDto();

        request.setOrderItems(List.of(item));

        return request;
    }

    private GetOrderResponseDto createOrderResponse() {

        GetOrderResponseDto response = new GetOrderResponseDto();

        response.setOrderId(100L);
        response.setUserId(1L);
        response.setTotalAmount(new BigDecimal("200.00"));
        response.setOrderStatus(OrderStatus.CREATED);
        response.setItems(Collections.emptyList());

        return response;
    }

    private void setAuthenticatedUser(Long userId) {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.emptyList()
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }
}