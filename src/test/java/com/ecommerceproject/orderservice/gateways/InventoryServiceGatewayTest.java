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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceGatewayTest {

    @Mock
    private InventoryServiceClient inventoryServiceClient;

    @InjectMocks
    private InventoryServiceGateway inventoryServiceGateway;


/*
 * =========================================================
 * RESERVE
 * =========================================================
 */

    @Test
    void reserveProduct_withValidRequest_shouldCallInventoryService() {

        InventoryResponseDto expectedResponse =
                new InventoryResponseDto();

        when(inventoryServiceClient.reserveProduct(
                org.mockito.ArgumentMatchers.any(
                        ReserveInventoryRequestDto.class
                )
        )).thenReturn(expectedResponse);

        InventoryResponseDto result =
                inventoryServiceGateway.reserveProduct(
                        10L,
                        3,
                        "reserve-123"
                );

        assertSame(
                expectedResponse,
                result
        );

        ArgumentCaptor<ReserveInventoryRequestDto> captor =
                ArgumentCaptor.forClass(
                        ReserveInventoryRequestDto.class
                );

        verify(inventoryServiceClient)
                .reserveProduct(
                        captor.capture()
                );

        ReserveInventoryRequestDto request =
                captor.getValue();

        assertEquals(
                10L,
                request.getProductId()
        );

        assertEquals(
                3,
                request.getQuantity()
        );

        assertEquals(
                "reserve-123",
                request.getOperationId()
        );
    }

    @Test
    void reserveProduct_whenInventoryReturnsBadRequest_shouldThrowInsufficientStockException() {

        FeignException.BadRequest badRequest =
                mock(FeignException.BadRequest.class);

        when(inventoryServiceClient.reserveProduct(
                org.mockito.ArgumentMatchers.any(
                        ReserveInventoryRequestDto.class
                )
        )).thenThrow(badRequest);

        InsufficientStockException exception =
                assertThrows(
                        InsufficientStockException.class,
                        () -> inventoryServiceGateway.reserveProduct(
                                10L,
                                20,
                                "reserve-123"
                        )
                );

        assertEquals(
                "Insufficient stock for product: 10",
                exception.getMessage()
        );
    }

    @Test
    void reserveProduct_whenInventoryServiceUnavailable_shouldThrowInventoryServiceUnavailableException() {

        FeignException.ServiceUnavailable serviceUnavailable =
                mock(FeignException.ServiceUnavailable.class);

        when(inventoryServiceClient.reserveProduct(
                org.mockito.ArgumentMatchers.any(
                        ReserveInventoryRequestDto.class
                )
        )).thenThrow(serviceUnavailable);

        InventoryServiceUnavailableException exception =
                assertThrows(
                        InventoryServiceUnavailableException.class,
                        () -> inventoryServiceGateway.reserveProduct(
                                10L,
                                3,
                                "reserve-123"
                        )
                );

        assertEquals(
                "Inventory service is temporarily unavailable",
                exception.getMessage()
        );
    }

    @Test
    void reserveProduct_whenUnexpectedFeignException_shouldThrowInventoryOperationException() {

        FeignException feignException =
                mock(FeignException.class);

        when(inventoryServiceClient.reserveProduct(
                org.mockito.ArgumentMatchers.any(
                        ReserveInventoryRequestDto.class
                )
        )).thenThrow(feignException);

        InventoryOperationException exception =
                assertThrows(
                        InventoryOperationException.class,
                        () -> inventoryServiceGateway.reserveProduct(
                                10L,
                                3,
                                "reserve-123"
                        )
                );

        assertEquals(
                "Unable to reserve inventory for product: 10",
                exception.getMessage()
        );
    }


/*
 * =========================================================
 * RELEASE
 * =========================================================
 */

    @Test
    void releaseProduct_withValidRequest_shouldCallInventoryService() {

        InventoryResponseDto expectedResponse =
                new InventoryResponseDto();

        when(inventoryServiceClient.releaseProduct(
                org.mockito.ArgumentMatchers.any(
                        ReleaseInventoryRequestDto.class
                )
        )).thenReturn(expectedResponse);

        InventoryResponseDto result =
                inventoryServiceGateway.releaseProduct(
                        10L,
                        3,
                        "release-123"
                );

        assertSame(
                expectedResponse,
                result
        );

        ArgumentCaptor<ReleaseInventoryRequestDto> captor =
                ArgumentCaptor.forClass(
                        ReleaseInventoryRequestDto.class
                );

        verify(inventoryServiceClient)
                .releaseProduct(
                        captor.capture()
                );

        ReleaseInventoryRequestDto request =
                captor.getValue();

        assertEquals(
                10L,
                request.getProductId()
        );

        assertEquals(
                3,
                request.getQuantity()
        );

        assertEquals(
                "release-123",
                request.getOperationId()
        );
    }

    @Test
    void releaseProduct_whenInventoryServiceUnavailable_shouldThrowInventoryServiceUnavailableException() {

        FeignException.ServiceUnavailable serviceUnavailable =
                mock(FeignException.ServiceUnavailable.class);

        when(inventoryServiceClient.releaseProduct(
                org.mockito.ArgumentMatchers.any(
                        ReleaseInventoryRequestDto.class
                )
        )).thenThrow(serviceUnavailable);

        InventoryServiceUnavailableException exception =
                assertThrows(
                        InventoryServiceUnavailableException.class,
                        () -> inventoryServiceGateway.releaseProduct(
                                10L,
                                3,
                                "release-123"
                        )
                );

        assertEquals(
                "Inventory service is temporarily unavailable",
                exception.getMessage()
        );
    }

    @Test
    void releaseProduct_whenUnexpectedFeignException_shouldThrowInventoryOperationException() {

        FeignException feignException =
                mock(FeignException.class);

        when(inventoryServiceClient.releaseProduct(
                org.mockito.ArgumentMatchers.any(
                        ReleaseInventoryRequestDto.class
                )
        )).thenThrow(feignException);

        InventoryOperationException exception =
                assertThrows(
                        InventoryOperationException.class,
                        () -> inventoryServiceGateway.releaseProduct(
                                10L,
                                3,
                                "release-123"
                        )
                );

        assertEquals(
                "Unable to release inventory for product: 10",
                exception.getMessage()
        );
    }


/*
 * =========================================================
 * COMMIT
 * =========================================================
 */

    @Test
    void commitProduct_withValidRequest_shouldCallInventoryService() {

        InventoryResponseDto expectedResponse =
                new InventoryResponseDto();

        when(inventoryServiceClient.commitProduct(
                org.mockito.ArgumentMatchers.any(
                        CommitInventoryRequestDto.class
                )
        )).thenReturn(expectedResponse);

        InventoryResponseDto result =
                inventoryServiceGateway.commitProduct(
                        10L,
                        3,
                        "commit-123"
                );

        assertSame(
                expectedResponse,
                result
        );

        ArgumentCaptor<CommitInventoryRequestDto> captor =
                ArgumentCaptor.forClass(
                        CommitInventoryRequestDto.class
                );

        verify(inventoryServiceClient)
                .commitProduct(
                        captor.capture()
                );

        CommitInventoryRequestDto request =
                captor.getValue();

        assertEquals(
                10L,
                request.getProductId()
        );

        assertEquals(
                3,
                request.getQuantity()
        );

        assertEquals(
                "commit-123",
                request.getOperationId()
        );
    }

    @Test
    void commitProduct_whenInventoryServiceUnavailable_shouldThrowInventoryServiceUnavailableException() {

        FeignException.ServiceUnavailable serviceUnavailable =
                mock(FeignException.ServiceUnavailable.class);

        when(inventoryServiceClient.commitProduct(
                org.mockito.ArgumentMatchers.any(
                        CommitInventoryRequestDto.class
                )
        )).thenThrow(serviceUnavailable);

        InventoryServiceUnavailableException exception =
                assertThrows(
                        InventoryServiceUnavailableException.class,
                        () -> inventoryServiceGateway.commitProduct(
                                10L,
                                3,
                                "commit-123"
                        )
                );

        assertEquals(
                "Inventory service is temporarily unavailable",
                exception.getMessage()
        );
    }

    @Test
    void commitProduct_whenUnexpectedFeignException_shouldThrowInventoryOperationException() {

        FeignException feignException =
                mock(FeignException.class);

        when(inventoryServiceClient.commitProduct(
                org.mockito.ArgumentMatchers.any(
                        CommitInventoryRequestDto.class
                )
        )).thenThrow(feignException);

        InventoryOperationException exception =
                assertThrows(
                        InventoryOperationException.class,
                        () -> inventoryServiceGateway.commitProduct(
                                10L,
                                3,
                                "commit-123"
                        )
                );

        assertEquals(
                "Unable to commit inventory for product: 10",
                exception.getMessage()
        );
    }
}