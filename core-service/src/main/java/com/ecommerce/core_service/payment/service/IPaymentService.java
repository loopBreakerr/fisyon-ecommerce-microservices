package com.ecommerce.core_service.payment.service;

import com.ecommerce.core_service.payment.model.PaymentModel;

import java.util.List;

public interface IPaymentService {

    List<PaymentModel> getPaymentsByOrderId(Long orderId, String userId, boolean isAdmin);
}
