package com.ecommerce.core_service.cart.service;

import com.ecommerce.core_service.cart.model.AddToCartRequest;
import com.ecommerce.core_service.cart.model.CartModel;

import java.util.List;

public interface ICartService {

    CartModel getMyCart(String userId);

    List<CartModel> getAllActiveCarts();

    CartModel addItemToCart(String userId, AddToCartRequest request);

    void removeItemFromCart(String userId, Long cartItemId);

    CartModel updateItemQuantity(String userId, Long cartItemId, int newQuantity);

    void clearCart(String userId);
}