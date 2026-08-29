package com.ecommerceproject.orderservice.gateways;

import com.ecommerceproject.orderservice.clients.ProductServiceClient;
import com.ecommerceproject.orderservice.dtos.responsedto.ProductResponseDto;
import com.ecommerceproject.orderservice.exceptions.ProductNotFoundException;
import feign.Feign;
import feign.FeignException;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceGateway {

    private final ProductServiceClient productServiceClient;

    public ProductServiceGateway(ProductServiceClient productServiceClient) {
        this.productServiceClient = productServiceClient;
    }

    public ProductResponseDto getProductById(Long productId) {
        try{
            return productServiceClient.getProductById(productId);
        } catch (FeignException.NotFound e) {
            throw new ProductNotFoundException("Product not found with id " + productId);
        }
    }
}
