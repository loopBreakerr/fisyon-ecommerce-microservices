package com.ecommerce.core_service.order.controller;

import com.ecommerce.core_service.order.model.CheckoutRequest;
import com.ecommerce.core_service.order.model.OrderModel;
import com.ecommerce.core_service.order.model.SellerOrderItemModel;
import com.ecommerce.core_service.order.service.IOrderService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class OrderController {

    private final IOrderService orderService;

    public OrderController(IOrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders/mine")
    public List<OrderModel> getMyOrders(@AuthenticationPrincipal Jwt jwt) {
        return orderService.getMyOrders(jwt.getSubject());
    }

    @GetMapping("/orders/{id}")
    public OrderModel getOrderById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return orderService.getOrderById(id, jwt.getSubject(), isAdmin(jwt));
    }

    @PostMapping("/orders/checkout")
    @PreAuthorize("!hasRole('seller') and !hasRole('admin')")
    public OrderModel checkout(@Valid @RequestBody CheckoutRequest request, @AuthenticationPrincipal Jwt jwt) {
        return orderService.checkout(jwt.getSubject(), request.getPaymentMethod());
    }

    @GetMapping("/orders/seller-items")
    @PreAuthorize("hasRole('seller')")
    public List<SellerOrderItemModel> getMyOrderItems(@AuthenticationPrincipal Jwt jwt) {
        return orderService.getOrderItemsBySeller(jwt.getSubject());
    }

    @SuppressWarnings("unchecked")
    private boolean isAdmin(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || realmAccess.get("roles") == null) {
            return false;
        }
        List<String> roles = (List<String>) realmAccess.get("roles");
        return roles.contains("admin");
    }
}
