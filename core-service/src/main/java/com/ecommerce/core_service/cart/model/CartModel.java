package com.ecommerce.core_service.cart.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CartModel {

    private final Long id;
    private final String userId;
    private final String status;
    private final List<CartItemModel> items;
    private final BigDecimal totalAmount;
    private final UUID uuid;
}
