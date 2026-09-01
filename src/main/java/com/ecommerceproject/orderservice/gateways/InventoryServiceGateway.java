package com.ecommerceproject.orderservice.gateways;

import com.ecommerceproject.orderservice.clients.InventoryServiceClient;
import com.ecommerceproject.orderservice.dtos.requestdto.CommitInventoryRequestDto;
import com.ecommerceproject.orderservice.dtos.requestdto.ReleaseInventoryRequestDto;
import com.ecommerceproject.orderservice.dtos.requestdto.ReserveInventoryRequestDto;
import com.ecommerceproject.orderservice.dtos.responsedto.InventoryResponseDto;
import com.ecommerceproject.orderservice.exceptions.InsufficientStockException;
import com.ecommerceproject.orderservice.exceptions.InventoryOperationException;
import com.ecommerceproject.orderservice.exceptions.InventoryServiceUnavailableException;
import feign.FeignException;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InventoryServiceGateway {

    private final InventoryServiceClient inventoryServiceClient;

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceGateway.class);

    public InventoryServiceGateway(InventoryServiceClient inventoryServiceClient) {
        this.inventoryServiceClient = inventoryServiceClient;
    }

    @Retry(name = "inventoryServiceRetry")
    public InventoryResponseDto reserveProduct(Long productId, Integer quantity, String operationId) {
        try {
            ReserveInventoryRequestDto requestDto = new ReserveInventoryRequestDto();

            requestDto.setProductId(productId);
            requestDto.setQuantity(quantity);
            requestDto.setOperationId(operationId);

            return inventoryServiceClient.reserveProduct(requestDto);
        } catch (FeignException.BadRequest e) {
            throw new InsufficientStockException("Insufficient stock for product: " + productId);
        } catch (FeignException.ServiceUnavailable e) {
            log.error(
                    "Inventory service unavailable during reserve. productId={}, quantity={}",
                    productId,
                    quantity,
                    e
            );
            throw new InventoryServiceUnavailableException("Inventory service is temporarily unavailable");
        } catch (FeignException e) {
            throw new InventoryOperationException("Unable to reserve inventory for product: " + productId);
        }
    }

    @Retry(name = "inventoryServiceRetry")
    public InventoryResponseDto releaseProduct(Long productId, Integer quantity, String operationId) {
        try {
            ReleaseInventoryRequestDto requestDto = new ReleaseInventoryRequestDto();

            requestDto.setProductId(productId);
            requestDto.setQuantity(quantity);
            requestDto.setOperationId(operationId);

            return inventoryServiceClient.releaseProduct(requestDto);
        } catch (FeignException.ServiceUnavailable e) {
            log.error(
                    "Inventory service unavailable during release. productId={}, quantity={}",
                    productId,
                    quantity,
                    e
            );
            throw new InventoryServiceUnavailableException("Inventory service is temporarily unavailable");
        } catch (FeignException e) {
            throw new InventoryOperationException("Unable to release inventory for product: " + productId);
        }
    }

    @Retry(name = "inventoryServiceRetry")
    public InventoryResponseDto commitProduct(Long productId, Integer quantity, String operationId) {
        try {
            CommitInventoryRequestDto requestDto = new CommitInventoryRequestDto();

            requestDto.setProductId(productId);
            requestDto.setQuantity(quantity);
            requestDto.setOperationId(operationId);

            return inventoryServiceClient.commitProduct(requestDto);
        } catch (FeignException.ServiceUnavailable e) {
            log.error(
                    "Inventory service unavailable during commit. productId={}, quantity={}",
                    productId,
                    quantity,
                    e
            );
            throw new InventoryServiceUnavailableException("Inventory service is temporarily unavailable");
        } catch (FeignException e) {
            throw new InventoryOperationException(
                    "Unable to commit inventory for product: " + productId
            );
        }
    }


}
