package com.ecommerce.client.internal.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private Long id;
    private String status;
    private List<CartItemResponse> items;
    private BigDecimal totalAmount;
}
