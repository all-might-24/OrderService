package com.ecommerceproject.orderservice.clients;

import com.ecommerceproject.orderservice.dtos.requestdto.CommitInventoryRequestDto;
import com.ecommerceproject.orderservice.dtos.requestdto.ReleaseInventoryRequestDto;
import com.ecommerceproject.orderservice.dtos.requestdto.ReserveInventoryRequestDto;
import com.ecommerceproject.orderservice.dtos.responsedto.InventoryResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "InventoryManagementService")
public interface InventoryServiceClient {

    @PostMapping("/inventory/reserve")
    InventoryResponseDto reserveProduct(@RequestBody ReserveInventoryRequestDto requestDto);

    @PostMapping("/inventory/release")
    InventoryResponseDto releaseProduct(@RequestBody ReleaseInventoryRequestDto requestDto);

    @PostMapping("/inventory/commit")
    InventoryResponseDto commitProduct(@RequestBody CommitInventoryRequestDto requestDto);

}
