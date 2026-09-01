package com.ecommerceproject.orderservice.gateways;

import com.ecommerceproject.orderservice.clients.ProductServiceClient;
import com.ecommerceproject.orderservice.dtos.responsedto.ProductResponseDto;
import com.ecommerceproject.orderservice.exceptions.ProductNotFoundException;
import com.ecommerceproject.orderservice.exceptions.ProductServiceUnavailableException;
import feign.Feign;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceGateway {

    private final ProductServiceClient productServiceClient;

    public ProductServiceGateway(ProductServiceClient productServiceClient) {
        this.productServiceClient = productServiceClient;
    }

    @Retry(
            name = "productServiceRetry",
            fallbackMethod = "getProductFallback"
    )
    @CircuitBreaker(
            name = "productServiceCircuitBreaker",
            fallbackMethod = "getProductFallback"
    )
    public ProductResponseDto getProductById(Long productId) {
        try{
            return productServiceClient.getProductById(productId);
        } catch (FeignException.NotFound e) {
            throw new ProductNotFoundException("Product not found with id " + productId);
        }
    }

    private ProductResponseDto getProductFallback(Long productId, Throwable throwable) {
        throw new ProductServiceUnavailableException("Product service is temporarily unavailable");
    }
}
