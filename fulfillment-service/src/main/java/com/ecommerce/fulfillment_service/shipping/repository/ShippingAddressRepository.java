package com.ecommerce.fulfillment_service.shipping.repository;

import com.ecommerce.fulfillment_service.shipping.entity.ShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {
}
