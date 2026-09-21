package com.ecommerce.core_service.cart.repository;

import com.ecommerce.core_service.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserIdAndStatus(String userId, String status);

    List<Cart> findByStatus(String status);
}