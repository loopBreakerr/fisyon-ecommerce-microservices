package com.ecommerce.core_service.cart.controller;

import com.ecommerce.core_service.cart.model.AddToCartRequest;
import com.ecommerce.core_service.cart.model.CartModel;
import com.ecommerce.core_service.cart.model.UpdateQuantityRequest;
import com.ecommerce.core_service.cart.service.ICartService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
public class CartController {

    private final ICartService cartService;

    public CartController(ICartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/cart")
    public CartModel getMyCart(@AuthenticationPrincipal Jwt jwt) {
        return cartService.getMyCart(jwt.getSubject());
    }

    @PostMapping("/cart/items")
    @PreAuthorize("!hasRole('seller') and !hasRole('admin')")
    public CartModel addItemToCart(@Valid @RequestBody AddToCartRequest request,
                                   @AuthenticationPrincipal Jwt jwt) {
        return cartService.addItemToCart(jwt.getSubject(), request);
    }

    @DeleteMapping("/cart/items/{cartItemId}")
    @PreAuthorize("!hasRole('seller') and !hasRole('admin')")
    public void removeItemFromCart(@PathVariable Long cartItemId,
                                   @AuthenticationPrincipal Jwt jwt) {
        cartService.removeItemFromCart(jwt.getSubject(), cartItemId);
    }

    @PutMapping("/cart/items/{cartItemId}")
    @PreAuthorize("!hasRole('seller') and !hasRole('admin')")
    public CartModel updateItemQuantity(@PathVariable Long cartItemId,
                                         @RequestBody UpdateQuantityRequest request,
                                         @AuthenticationPrincipal Jwt jwt) {
        return cartService.updateItemQuantity(jwt.getSubject(), cartItemId, request.getQuantity());
    }

    @DeleteMapping("/cart/clear")
    @PreAuthorize("!hasRole('seller') and !hasRole('admin')")
    public void clearCart(@AuthenticationPrincipal Jwt jwt) {
        cartService.clearCart(jwt.getSubject());
    }
}