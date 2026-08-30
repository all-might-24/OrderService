package com.ecommerceproject.orderservice.exceptions;

public class InvalidOrderStateException extends RuntimeException{
    public InvalidOrderStateException(String message) {
        super(message);
    }
}
