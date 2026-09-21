package com.ecommerce.core_service.payment.repository;

import com.ecommerce.core_service.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
}
