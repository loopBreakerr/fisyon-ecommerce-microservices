package com.ecommerce.client.internal.cart;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "cart-service", url = "http://localhost:8081")
public interface ICartClient {

    @GetMapping("/cart")
    CartResponse getMyCart();

    @DeleteMapping("/cart/clear")
    void clearCart();
}
