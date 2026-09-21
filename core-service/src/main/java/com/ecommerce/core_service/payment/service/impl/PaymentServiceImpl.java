package com.ecommerce.core_service.payment.service.impl;

import com.ecommerce.client.internal.order.IOrderClient;
import com.ecommerce.client.internal.order.OrderResponse;
import com.ecommerce.core_service.payment.entity.Payment;
import com.ecommerce.core_service.payment.model.PaymentModel;
import com.ecommerce.core_service.payment.repository.IPaymentRepository;
import com.ecommerce.core_service.payment.service.IPaymentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentServiceImpl implements IPaymentService {

    private final IPaymentRepository paymentRepository;
    private final IOrderClient orderClient;

    public PaymentServiceImpl(IPaymentRepository paymentRepository, IOrderClient orderClient) {
        this.paymentRepository = paymentRepository;
        this.orderClient = orderClient;
    }

    @Override
    public List<PaymentModel> getPaymentsByOrderId(Long orderId, String userId, boolean isAdmin) {
        // Feign cagrisi, cagiranin JWT'sini otomatik tasidigi icin
        // orders/{id} zaten kendi ownership kontrolunu yapiyor - ama burada da acikca
        // kontrol ediyoruz (cifte koruma, sadece dolayli Feign reddine guvenmiyoruz).
        OrderResponse order = orderClient.getOrderById(orderId);
        if (!isAdmin && !order.getUserId().equals(userId)) {
            throw new SecurityException("Bu ödeme bilgisine erişim yetkiniz yok");
        }

        return paymentRepository.findByOrderId(orderId)
                .stream()
                .map(this::toModel)
                .toList();
    }

    private PaymentModel toModel(Payment payment) {
        return new PaymentModel(
                payment.getId(),
                payment.getUuid(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getTransactionId(),
                payment.getCreatedAt()
        );
    }
}
