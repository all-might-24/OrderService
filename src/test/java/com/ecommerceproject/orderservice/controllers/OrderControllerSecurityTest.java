package com.ecommerceproject.orderservice.controllers;

import com.ecommerceproject.orderservice.configs.SecurityConfig;
import com.ecommerceproject.orderservice.dtos.requestdto.CreateOrderRequestDto;
import com.ecommerceproject.orderservice.dtos.requestdto.OrderItemsRequestDto;
import com.ecommerceproject.orderservice.dtos.responsedto.CreateOrderResponseDto;
import com.ecommerceproject.orderservice.dtos.responsedto.GetOrderResponseDto;
import com.ecommerceproject.orderservice.models.enums.OrderStatus;
import com.ecommerceproject.orderservice.security.JwtAuthenticationFilter;
import com.ecommerceproject.orderservice.services.IOrderService;
import com.ecommerceproject.orderservice.services.ITokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        OrderControllerSecurityTest.TestSecurityConfiguration.class
})
class OrderControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IOrderService orderService;

    @MockitoBean
    private ITokenService tokenService;

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfiguration {
    }
/*
 * =========================================================
 * CREATE ORDER
 *
 * SecurityConfig:
 * POST /orders -> ROLE_USER
 * =========================================================
 */

    @Test
    void createOrder_withoutAuthentication_shouldReturn401() throws Exception {

        CreateOrderRequestDto request = createValidRequest();

        mockMvc.perform(
                        post("/orders")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized());

        verify(orderService, never())
                .createOrder(any(), any());
    }

    @Test
    void createOrder_withInvalidToken_shouldReturn401() throws Exception {

        CreateOrderRequestDto request = createValidRequest();

        when(tokenService.validateToken("invalid-token"))
                .thenReturn(false);

        mockMvc.perform(
                        post("/orders")
                                .header(
                                        "Authorization",
                                        "Bearer invalid-token"
                                )
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized());

        verify(orderService, never())
                .createOrder(any(), any());
    }

    @Test
    void createOrder_asUser_shouldReturn201() throws Exception {

        mockUserToken(1L);

        CreateOrderRequestDto request = createValidRequest();

        CreateOrderResponseDto response =
                new CreateOrderResponseDto();

        response.setOrderId(100L);
        response.setOrderStatus(OrderStatus.CREATED);
        response.setTotalAmount(
                new BigDecimal("200.00")
        );

        when(orderService.createOrder(eq(1L), any(CreateOrderRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/orders")
                                .header(
                                        "Authorization",
                                        "Bearer user-token"
                                )
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(100))
                .andExpect(jsonPath("$.orderStatus").value("CREATED"));

        verify(orderService).createOrder(eq(1L), any(CreateOrderRequestDto.class));
    }

    @Test
    void createOrder_asAdmin_shouldReturn403() throws Exception {

        mockAdminToken(99L);

        CreateOrderRequestDto request = createValidRequest();

        mockMvc.perform(
                        post("/orders")
                                .header(
                                        "Authorization",
                                        "Bearer admin-token"
                                )
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isForbidden());

        verify(orderService, never())
                .createOrder(any(), any());
    }


    /*
     * =========================================================
     * GET ORDER
     *
     * SecurityConfig:
     * GET /orders/** -> ROLE_USER
     * =========================================================
     */

    @Test
    void getOrder_withoutAuthentication_shouldReturn401()
            throws Exception {

        mockMvc.perform(
                        get("/orders/100")
                )
                .andExpect(status().isUnauthorized());

        verify(orderService, never())
                .getOrderById(any(), any());
    }

    @Test
    void getOrder_asUser_shouldReturn200() throws Exception {

        mockUserToken(1L);

        GetOrderResponseDto response =
                createOrderResponse(OrderStatus.CREATED);

        when(orderService.getOrderById(1L, 100L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/orders/100")
                                .header(
                                        "Authorization",
                                        "Bearer user-token"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.orderId")
                                .value(100)
                );

        verify(orderService)
                .getOrderById(1L, 100L);
    }

    @Test
    void getOrder_asAdmin_shouldReturn403() throws Exception {

        mockAdminToken(99L);

        mockMvc.perform(
                        get("/orders/100")
                                .header(
                                        "Authorization",
                                        "Bearer admin-token"
                                )
                )
                .andExpect(status().isForbidden());

        verify(orderService, never())
                .getOrderById(any(), any());
    }


    /*
     * =========================================================
     * GET MY ORDERS
     * =========================================================
     */

    @Test
    void getMyOrders_withoutAuthentication_shouldReturn401() throws Exception {

        mockMvc.perform(
                        get("/orders/my-orders")
                )
                .andExpect(status().isUnauthorized());

        verify(orderService, never())
                .getAllOrders(any(), any());
    }

    @Test
    void getMyOrders_asUser_shouldReturn200() throws Exception {

        mockUserToken(1L);

        when(orderService.getAllOrders(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(
                        get("/orders/my-orders")
                                .header(
                                        "Authorization",
                                        "Bearer user-token"
                                )
                )
                .andExpect(status().isOk());

        verify(orderService)
                .getAllOrders(eq(1L), any());
    }


    /*
     * =========================================================
     * CANCEL ORDER
     *
     * SecurityConfig:
     * POST /orders/../cancel -> ROLE_USER
     * =========================================================
             */

    @Test
    void cancelOrder_withoutAuthentication_shouldReturn401() throws Exception {

        mockMvc.perform(
                        post("/orders/100/cancel")
                )
                .andExpect(status().isUnauthorized());

        verify(orderService, never())
                .cancelOrder(any(), any());
    }

    @Test
    void cancelOrder_asUser_shouldReturn200() throws Exception {

        mockUserToken(1L);

        GetOrderResponseDto response = createOrderResponse(OrderStatus.CANCELLED);

        when(orderService.cancelOrder(1L, 100L))
                .thenReturn(response);

        mockMvc.perform(
                        post("/orders/100/cancel")
                                .header(
                                        "Authorization",
                                        "Bearer user-token"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.orderStatus")
                                .value("CANCELLED")
                );

        verify(orderService)
                .cancelOrder(1L, 100L);
    }

    @Test
    void cancelOrder_asAdmin_shouldReturn403() throws Exception {

        mockAdminToken(99L);

        mockMvc.perform(
                        post("/orders/100/cancel")
                                .header(
                                        "Authorization",
                                        "Bearer admin-token"
                                )
                )
                .andExpect(status().isForbidden());

        verify(orderService, never())
                .cancelOrder(any(), any());
    }


    /*
     * =========================================================
     * SHIP ORDER
     *
     * SecurityConfig:
     * POST /orders/../ship -> ROLE_ADMIN
     * =========================================================
             */

    @Test
    void shipOrder_withoutAuthentication_shouldReturn401() throws Exception {

        mockMvc.perform(
                        post("/orders/100/ship")
                )
                .andExpect(status().isUnauthorized());

        verify(orderService, never())
                .shipOrder(any());
    }

    @Test
    void shipOrder_asUser_shouldReturn403() throws Exception {

        mockUserToken(1L);

        mockMvc.perform(
                        post("/orders/100/ship")
                                .header(
                                        "Authorization",
                                        "Bearer user-token"
                                )
                )
                .andExpect(status().isForbidden());

        verify(orderService, never())
                .shipOrder(any());
    }

    @Test
    void shipOrder_asAdmin_shouldReturn200() throws Exception {

        mockAdminToken(99L);

        GetOrderResponseDto response = createOrderResponse(OrderStatus.SHIPPED);

        when(orderService.shipOrder(100L))
                .thenReturn(response);

        mockMvc.perform(
                        post("/orders/100/ship")
                                .header(
                                        "Authorization",
                                        "Bearer admin-token"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.orderStatus")
                                .value("SHIPPED")
                );

        verify(orderService)
                .shipOrder(100L);
    }


    /*
     * =========================================================
     * DELIVER ORDER
     *
     * SecurityConfig:
     * POST /orders/../deliver -> ROLE_ADMIN
     * =========================================================
             */

    @Test
    void deliverOrder_withoutAuthentication_shouldReturn401() throws Exception {

        mockMvc.perform(
                        post("/orders/100/deliver")
                )
                .andExpect(status().isUnauthorized());

        verify(orderService, never())
                .deliverOrder(any());
    }

    @Test
    void deliverOrder_asUser_shouldReturn403() throws Exception {

        mockUserToken(1L);

        mockMvc.perform(
                        post("/orders/100/deliver")
                                .header(
                                        "Authorization",
                                        "Bearer user-token"
                                )
                )
                .andExpect(status().isForbidden());

        verify(orderService, never())
                .deliverOrder(any());
    }

    @Test
    void deliverOrder_asAdmin_shouldReturn200() throws Exception {

        mockAdminToken(99L);

        GetOrderResponseDto response = createOrderResponse(OrderStatus.DELIVERED);

        when(orderService.deliverOrder(100L))
                .thenReturn(response);

        mockMvc.perform(
                        post("/orders/100/deliver")
                                .header(
                                        "Authorization",
                                        "Bearer admin-token"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.orderStatus")
                                .value("DELIVERED")
                );

        verify(orderService)
                .deliverOrder(100L);
    }


/*
 * =========================================================
 * TOKEN / AUTHENTICATION EDGE CASES
 * =========================================================
 */

    @Test
    void request_withAuthorizationHeaderWithoutBearer_shouldReturn401()
            throws Exception {

        mockMvc.perform(
                        get("/orders/100")
                                .header(
                                        "Authorization",
                                        "user-token"
                                )
                )
                .andExpect(status().isUnauthorized());

        verify(tokenService, never())
                .validateToken(any());

        verify(orderService, never())
                .getOrderById(any(), any());
    }


/*
 * =========================================================
 * HELPERS
 * =========================================================
 */

    private void mockUserToken(Long userId) {

        Claims claims = createClaims(
                userId,
                List.of("USER")
        );

        when(tokenService.validateToken("user-token"))
                .thenReturn(true);

        when(tokenService.getPayload("user-token"))
                .thenReturn(claims);
    }

    private void mockAdminToken(Long userId) {

        Claims claims = createClaims(
                userId,
                List.of("ADMIN")
        );

        when(tokenService.validateToken("admin-token"))
                .thenReturn(true);

        when(tokenService.getPayload("admin-token"))
                .thenReturn(claims);
    }

    private Claims createClaims(
            Long userId,
            List<String> roles) {

        return new DefaultClaims(
                Map.of(
                        "userId", userId,
                        "scope", roles
                )
        );
    }

    private CreateOrderRequestDto createValidRequest() {

        OrderItemsRequestDto item = new OrderItemsRequestDto();

        item.setProductId(10L);
        item.setQuantity(2);

        CreateOrderRequestDto request = new CreateOrderRequestDto();

        request.setOrderItems(List.of(item));

        return request;
    }

    private GetOrderResponseDto createOrderResponse(OrderStatus status) {

        GetOrderResponseDto response = new GetOrderResponseDto();

        response.setOrderId(100L);
        response.setUserId(1L);
        response.setTotalAmount(new BigDecimal("200.00"));
        response.setOrderStatus(status);
        response.setItems(List.of());

        return response;
    }
}