package com.ecommerceproject.orderservice.clients;

import com.ecommerceproject.orderservice.dtos.responsedto.ProductResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ProductService")
public interface ProductServiceClient {

    @GetMapping("/products/{id}")
    ProductResponseDto getProductById(@PathVariable("id") Long productId);
}
