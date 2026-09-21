package com.ecommerce.core_service.order.repository;

import com.ecommerce.core_service.order.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IOrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
}
