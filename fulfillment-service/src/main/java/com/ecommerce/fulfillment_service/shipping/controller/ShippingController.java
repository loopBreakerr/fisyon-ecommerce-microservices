package com.ecommerce.fulfillment_service.shipping.controller;

import com.ecommerce.fulfillment_service.shipping.model.ShipmentModel;
import com.ecommerce.fulfillment_service.shipping.service.ShippingService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ShippingController {

    private final ShippingService shippingService;

    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @GetMapping("/shipping/order/{orderId}")
    public ShipmentModel getShipmentByOrderId(@PathVariable Long orderId, @AuthenticationPrincipal Jwt jwt) {
        return shippingService.getShipmentByOrderId(orderId, jwt.getSubject(), isAdmin(jwt), jwt.getTokenValue());
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
