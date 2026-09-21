package com.ecommerce.fulfillment_service.shipping.repository;

import com.ecommerce.fulfillment_service.shipping.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByOrderId(Long orderId);
}
