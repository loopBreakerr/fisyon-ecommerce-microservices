package com.ecommerce.fulfillment_service.shipping.service.impl;

import com.ecommerce.fulfillment_client.order.OrderClient;
import com.ecommerce.fulfillment_client.order.OrderResponse;
import com.ecommerce.fulfillment_service.shipping.entity.Shipment;
import com.ecommerce.fulfillment_service.shipping.model.ShipmentModel;
import com.ecommerce.fulfillment_service.shipping.repository.ShipmentRepository;
import com.ecommerce.fulfillment_service.shipping.service.ShippingService;
import org.springframework.stereotype.Service;

@Service
public class ShippingServiceImpl implements ShippingService {

    private final ShipmentRepository shipmentRepository;
    private final OrderClient orderClient;

    public ShippingServiceImpl(ShipmentRepository shipmentRepository, OrderClient orderClient) {
        this.shipmentRepository = shipmentRepository;
        this.orderClient = orderClient;
    }

    @Override
    public ShipmentModel getShipmentByOrderId(Long orderId, String userId, boolean isAdmin, String jwtToken) {
        OrderResponse order = orderClient.getOrderById(orderId, jwtToken);
        if (!isAdmin && !order.getUserId().equals(userId)) {
            throw new SecurityException("Bu kargo bilgisine erişim yetkiniz yok");
        }

        Shipment shipment = shipmentRepository.findByOrderId(orderId).orElseThrow();
        return toModel(shipment);
    }

    private ShipmentModel toModel(Shipment shipment) {
        return new ShipmentModel(
                shipment.getId(),
                shipment.getUuid(),
                shipment.getOrderId(),
                shipment.getStatus(),
                shipment.getCarrier(),
                shipment.getTrackingNumber(),
                shipment.getShippedAt(),
                shipment.getDeliveredAt(),
                shipment.getCreatedAt()
        );
    }
}
