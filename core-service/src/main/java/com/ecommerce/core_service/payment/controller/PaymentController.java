package com.ecommerce.core_service.payment.controller;

import com.ecommerce.core_service.payment.model.PaymentModel;
import com.ecommerce.core_service.payment.service.IPaymentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class PaymentController {

    private final IPaymentService paymentService;

    public PaymentController(IPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/payments/order/{orderId}")
    public List<PaymentModel> getPaymentsByOrderId(@PathVariable Long orderId, @AuthenticationPrincipal Jwt jwt) {
        return paymentService.getPaymentsByOrderId(orderId, jwt.getSubject(), isAdmin(jwt));
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
