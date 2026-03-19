package com.example.apipayment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="ordersFeignClient", url="http://localhost:8081")
public interface OrdersFeignClient {

    @GetMapping("/orders/{ordersIdx}")
    public PaymentDto.OrdersRes getOrders(@PathVariable Long ordersIdx);
}
