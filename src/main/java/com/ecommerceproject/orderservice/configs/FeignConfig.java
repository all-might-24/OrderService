package com.ecommerceproject.orderservice.configs;


import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(
        basePackages = "com.ecommerceproject.orderservice.clients"
)
public class FeignConfig {
}