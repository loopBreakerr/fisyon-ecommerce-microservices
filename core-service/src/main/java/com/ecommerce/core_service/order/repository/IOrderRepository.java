package com.ecommerce.core_service.order.repository;

import com.ecommerce.core_service.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IOrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Order> findAllByOrderByCreatedAtDesc();
}
