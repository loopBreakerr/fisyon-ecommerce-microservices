package com.ecommerce.core_service.payment.listener;

import com.ecommerce.core_service.payment.entity.Payment;
import com.ecommerce.core_service.payment.entity.PaymentTransaction;
import com.ecommerce.core_service.payment.event.OrderCreatedEvent;
import com.ecommerce.core_service.payment.event.PaymentCompletedEvent;
import com.ecommerce.core_service.payment.event.PaymentEventPublisher;
import com.ecommerce.core_service.payment.repository.IPaymentRepository;
import com.ecommerce.core_service.payment.repository.IPaymentTransactionRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("paymentOrderEventListener")
public class OrderEventListener {

    private final IPaymentRepository paymentRepository;
    private final IPaymentTransactionRepository paymentTransactionRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    public OrderEventListener(IPaymentRepository paymentRepository,
                              IPaymentTransactionRepository paymentTransactionRepository,
                              PaymentEventPublisher paymentEventPublisher) {
        this.paymentRepository = paymentRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    @KafkaListener(topics = "order-events", groupId = "payment-service-group", containerFactory = "paymentOrderEventsContainerFactory")
    public void handleOrderCreated(OrderCreatedEvent event) {
        // 1. Gelen event'ten yeni bir Payment olustur
        Payment payment = new Payment();
        payment.setOrderId(event.getOrderId());
        payment.setAmount(event.getTotalAmount());
        payment.setStatus("PENDING");
        payment.setPaymentMethod(event.getPaymentMethod());
        payment.setTransactionId(null);

        // 2. Kaydet
        payment = paymentRepository.save(payment);

        // 3. Kullanici checkout'ta odeme yontemini zaten secip onayladigi icin
        // (tek adimli manuel onay akisi) - odeme deterministik olarak basarili sayilir,
        // rastgele basarisizlik simulasyonu yok.
        String finalStatus = "SUCCESS";
        payment.setStatus(finalStatus);
        payment.setTransactionId(UUID.randomUUID().toString());
        paymentRepository.save(payment);

        // 4. PaymentTransaction kaydi olustur
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setPaymentId(payment.getId());
        transaction.setType("CHARGE");
        transaction.setStatus(finalStatus);
        transaction.setAmount(payment.getAmount());
        paymentTransactionRepository.save(transaction);

        // 5. Konsola log
        System.out.println("Payment processed for order " + event.getOrderId() + ": " + finalStatus);

        // 6. payment-events'e PaymentCompletedEvent yayinla
        PaymentCompletedEvent completedEvent = new PaymentCompletedEvent();
        completedEvent.setOrderId(payment.getOrderId());
        completedEvent.setPaymentId(payment.getId());
        completedEvent.setStatus(payment.getStatus());
        completedEvent.setAmount(payment.getAmount());
        completedEvent.setTransactionId(payment.getTransactionId());
        completedEvent.setUserId(event.getUserId());
        paymentEventPublisher.publishPaymentCompleted(completedEvent);
    }
}
