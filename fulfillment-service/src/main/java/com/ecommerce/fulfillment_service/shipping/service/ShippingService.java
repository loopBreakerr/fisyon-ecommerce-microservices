package com.ecommerce.fulfillment_service.shipping.service;

import com.ecommerce.fulfillment_service.shipping.model.ShipmentModel;

public interface ShippingService {

    ShipmentModel getShipmentByOrderId(Long orderId, String userId, boolean isAdmin, String jwtToken);
}
