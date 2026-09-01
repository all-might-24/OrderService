package com.ecommerceproject.orderservice.gateways;

import com.ecommerceproject.orderservice.clients.ProductServiceClient;
import com.ecommerceproject.orderservice.dtos.responsedto.ProductResponseDto;
import com.ecommerceproject.orderservice.exceptions.ProductNotFoundException;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceGatewayTest {

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private ProductServiceGateway productServiceGateway;


    /*
     * =========================================================
     * GET PRODUCT
     * =========================================================
     */

    @Test
    void getProductById_whenProductExists_shouldReturnProduct() {

        ProductResponseDto product =
                new ProductResponseDto();

        product.setId(10L);
        product.setTitle("Gaming Laptop");
        product.setPrice(
                new BigDecimal("1500.00")
        );
        product.setQty(10);

        when(productServiceClient.getProductById(10L))
                .thenReturn(product);

        ProductResponseDto result =
                productServiceGateway.getProductById(10L);

        assertSame(
                product,
                result
        );

        assertEquals(
                10L,
                result.getId()
        );

        assertEquals(
                "Gaming Laptop",
                result.getTitle()
        );

        assertEquals(
                new BigDecimal("1500.00"),
                result.getPrice()
        );

        verify(productServiceClient)
                .getProductById(10L);
    }

    @Test
    void getProductById_whenProductNotFound_shouldThrowProductNotFoundException() {

        FeignException.NotFound notFound =
                mock(FeignException.NotFound.class);

        when(productServiceClient.getProductById(10L))
                .thenThrow(notFound);

        ProductNotFoundException exception =
                assertThrows(
                        ProductNotFoundException.class,
                        () -> productServiceGateway.getProductById(10L)
                );

        assertEquals(
                "Product not found with id 10",
                exception.getMessage()
        );

        verify(productServiceClient)
                .getProductById(10L);
    }

    @Test
    void getProductById_whenUnexpectedFeignException_shouldPropagateExceptionInUnitTest() {

        FeignException feignException =
                mock(FeignException.class);

        when(productServiceClient.getProductById(10L))
                .thenThrow(feignException);

        FeignException thrown =
                assertThrows(
                        FeignException.class,
                        () -> productServiceGateway.getProductById(10L)
                );

        assertSame(
                feignException,
                thrown
        );
    }
}