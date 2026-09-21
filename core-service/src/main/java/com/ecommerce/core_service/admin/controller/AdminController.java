package com.ecommerce.core_service.admin.controller;

import com.ecommerce.core_service.admin.model.CustomerSummaryModel;
import com.ecommerce.core_service.admin.model.SellerSummaryModel;
import com.ecommerce.core_service.admin.service.IAdminUserService;
import com.ecommerce.core_service.cart.model.CartModel;
import com.ecommerce.core_service.cart.service.ICartService;
import com.ecommerce.core_service.order.model.OrderModel;
import com.ecommerce.core_service.order.service.IOrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * cart, order ve kullanici (Keycloak) domain'lerine ait admin-only raporlama
 * uc noktalari. Tek bir paket altinda toplandi (cart/order'a dagitilmadi) -
 * birden fazla domain'in verisini bir arada gosteren, domain'e ozgu olmayan
 * bir "admin" katmani oldugu icin ayri bir paket daha tutarli.
 */
@RestController
public class AdminController {

    private final ICartService cartService;
    private final IOrderService orderService;
    private final IAdminUserService adminUserService;

    public AdminController(ICartService cartService, IOrderService orderService, IAdminUserService adminUserService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.adminUserService = adminUserService;
    }

    @GetMapping("/admin/carts")
    @PreAuthorize("hasRole('admin')")
    public List<CartModel> getAllCarts() {
        return cartService.getAllActiveCarts();
    }

    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('admin')")
    public List<OrderModel> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/admin/sellers")
    @PreAuthorize("hasRole('admin')")
    public List<SellerSummaryModel> getAllSellers() {
        return adminUserService.getAllSellers();
    }

    @GetMapping("/admin/customers")
    @PreAuthorize("hasRole('admin')")
    public List<CustomerSummaryModel> getAllCustomers() {
        return adminUserService.getAllCustomers();
    }
}
