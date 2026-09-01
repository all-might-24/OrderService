package com.ecommerceproject.orderservice.exceptions;

public class InventoryServiceUnavailableException extends RuntimeException{
    public InventoryServiceUnavailableException(String message) {
        super(message);
    }
}
