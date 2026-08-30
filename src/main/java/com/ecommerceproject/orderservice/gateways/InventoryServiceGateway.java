package com.ecommerceproject.orderservice.gateways;

import com.ecommerceproject.orderservice.clients.InventoryServiceClient;
import com.ecommerceproject.orderservice.dtos.requestdto.CommitInventoryRequestDto;
import com.ecommerceproject.orderservice.dtos.requestdto.ReleaseInventoryRequestDto;
import com.ecommerceproject.orderservice.dtos.requestdto.ReserveInventoryRequestDto;
import com.ecommerceproject.orderservice.dtos.responsedto.InventoryResponseDto;
import com.ecommerceproject.orderservice.exceptions.InsufficientStockException;
import feign.FeignException;
import org.springframework.stereotype.Service;

@Service
public class InventoryServiceGateway {

    private final InventoryServiceClient inventoryServiceClient;

    public InventoryServiceGateway(InventoryServiceClient inventoryServiceClient) {
        this.inventoryServiceClient = inventoryServiceClient;
    }

    public InventoryResponseDto reserveProduct(Long productId, Integer quantity) {
        try {
            ReserveInventoryRequestDto requestDto = new ReserveInventoryRequestDto();

            requestDto.setProductId(productId);
            requestDto.setQuantity(quantity);

            return inventoryServiceClient.reserveProduct(requestDto);
        } catch (FeignException.BadRequest e) {
            throw new InsufficientStockException("Insufficient stock for product: " + productId);
        }
    }

    public InventoryResponseDto releaseProduct(Long productId, Integer quantity) {
        ReleaseInventoryRequestDto requestDto = new ReleaseInventoryRequestDto();

        requestDto.setProductId(productId);
        requestDto.setQuantity(quantity);

        return inventoryServiceClient.releaseProduct(requestDto);
    }

    public InventoryResponseDto commitProduct(Long productId, Integer quantity) {
       CommitInventoryRequestDto requestDto = new CommitInventoryRequestDto();

        requestDto.setProductId(productId);
        requestDto.setQuantity(quantity);

        return inventoryServiceClient.commitProduct(requestDto);
    }


}
