package com.ecommerce.client.internal.order;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service-payment", url = "http://localhost:8081")
public interface IOrderClient {

    @GetMapping("/orders/{id}")
    OrderResponse getOrderById(@PathVariable("id") Long id);
}
