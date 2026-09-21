package com.ecommerce.core_service.order.listener;

import com.ecommerce.core_service.order.entity.Order;
import com.ecommerce.core_service.order.entity.OrderStatusHistory;
import com.ecommerce.core_service.order.event.PaymentCompletedEvent;
import com.ecommerce.core_service.order.repository.IOrderRepository;
import com.ecommerce.core_service.order.repository.IOrderStatusHistoryRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPaymentEventListener {

    private final IOrderRepository orderRepository;
    private final IOrderStatusHistoryRepository orderStatusHistoryRepository;

    public OrderPaymentEventListener(IOrderRepository orderRepository,
                                     IOrderStatusHistoryRepository orderStatusHistoryRepository) {
        this.orderRepository = orderRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }

    @KafkaListener(topics = "payment-events", groupId = "order-service-group", containerFactory = "orderPaymentEventsContainerFactory")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        // a. Order'i bul
        Order order = orderRepository.findById(event.getOrderId()).orElseThrow();

        // b. odeme her zaman deterministik olarak basarili sayildigi icin (
        // payment.listener.OrderEventListener) yeni durum sabit olarak PAID
        String newStatus = "PAID";
        order.setStatus(newStatus);

        // c. Kaydet
        orderRepository.save(order);

        // d. OrderStatusHistory kaydi olustur
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrderId(order.getId());
        history.setStatus(newStatus);
        history.setNote("Payment " + event.getStatus() + ", transaction: " + event.getTransactionId());
        orderStatusHistoryRepository.save(history);

        // e. Konsola log
        System.out.println("Order " + event.getOrderId() + " status updated to " + newStatus);
    }
}
