package com.ecommerce.fulfillment_service.shipping.listener;

import com.ecommerce.fulfillment_service.shipping.entity.Shipment;
import com.ecommerce.fulfillment_service.shipping.event.PaymentCompletedEvent;
import com.ecommerce.fulfillment_service.shipping.repository.ShipmentRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("shippingEventListener")
public class ShippingEventListener {

    private final ShipmentRepository shipmentRepository;

    public ShippingEventListener(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @KafkaListener(topics = "payment-events", groupId = "fulfillment-service-group",
            containerFactory = "shippingPaymentEventsContainerFactory")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        if (!"SUCCESS".equals(event.getStatus())) {
            return;
        }

        Shipment shipment = new Shipment();
        shipment.setOrderId(event.getOrderId());
        shipment.setStatus("PREPARING");
        shipment.setCarrier("MOCK_CARRIER");
        shipment.setTrackingNumber(UUID.randomUUID().toString());
        shipmentRepository.save(shipment);

        System.out.println("Shipment created for order " + event.getOrderId());
    }
}
