package com.ecommerce.core_service.order.repository;

import com.ecommerce.core_service.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IOrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderIdOrderByIdAsc(Long orderId);
    List<OrderItem> findBySellerId(String sellerId);
}
