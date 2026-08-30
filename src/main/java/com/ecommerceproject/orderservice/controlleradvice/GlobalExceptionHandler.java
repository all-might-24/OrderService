package com.ecommerceproject.orderservice.controlleradvice;

import com.ecommerceproject.orderservice.dtos.responsedto.ExceptionDto;
import com.ecommerceproject.orderservice.exceptions.InsufficientStockException;
import com.ecommerceproject.orderservice.exceptions.InvalidOrderStateException;
import com.ecommerceproject.orderservice.exceptions.OrderNotFoundException;
import com.ecommerceproject.orderservice.exceptions.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ExceptionDto> handleProductNotFoundException( ProductNotFoundException e) {
        int status = HttpStatus.NOT_FOUND.value();
        ExceptionDto dto = createExceptionDto(status, e.getMessage());
        return ResponseEntity
                .status(status)
                .body(dto);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ExceptionDto> handleOrderNotFoundException(Exception e) {
        int status = HttpStatus.NOT_FOUND.value();
        ExceptionDto dto = createExceptionDto(status, e.getMessage());
        return ResponseEntity
                .status(status)
                .body(dto);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ExceptionDto> handleInsufficientStockException(InsufficientStockException e) {
        int status = HttpStatus.BAD_REQUEST.value();
        ExceptionDto dto = createExceptionDto(status, e.getMessage());
        return ResponseEntity
                .status(status)
                .body(dto);
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<ExceptionDto> handleInvalidOrderStateException (InvalidOrderStateException  e) {
        int status = HttpStatus.BAD_REQUEST.value();
        ExceptionDto dto = createExceptionDto(status, e.getMessage());
        return ResponseEntity
                .status(status)
                .body(dto);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errors =  e
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, error ->error.getDefaultMessage()));
        int status = HttpStatus.BAD_REQUEST.value();
        ExceptionDto dto = createExceptionDto(status, "Validation Failed");
        dto.setErrors(errors);
        return ResponseEntity
                .status(status)
                .body(dto);

    }

    private ExceptionDto createExceptionDto(int status, String message) {
        ExceptionDto dto = new ExceptionDto();

        dto.setStatus(status);
        dto.setMessage(message);
        dto.setTimeStamp(LocalDateTime.now());

        return dto;
    }

}

