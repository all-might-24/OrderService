package com.ecommerceproject.orderservice.exceptions;

public class InventoryOperationException extends RuntimeException{
    public InventoryOperationException(String message) {
        super(message);
    }
}
