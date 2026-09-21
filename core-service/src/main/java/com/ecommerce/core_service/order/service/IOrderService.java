package com.ecommerce.core_service.order.service;

import com.ecommerce.core_service.order.model.OrderModel;
import com.ecommerce.core_service.order.model.PaymentMethod;
import com.ecommerce.core_service.order.model.SellerOrderItemModel;

import java.util.List;

public interface IOrderService {

    List<OrderModel> getMyOrders(String userId);

    List<OrderModel> getAllOrders();

    OrderModel getOrderById(Long id, String userId, boolean isAdmin);

    OrderModel checkout(String userId, PaymentMethod paymentMethod);

    List<SellerOrderItemModel> getOrderItemsBySeller(String sellerId);
}
