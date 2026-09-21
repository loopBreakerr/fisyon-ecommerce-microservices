package com.ecommerce.core_service.catalog.product.repository;

import com.ecommerce.core_service.catalog.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByUuid(UUID uuid);

    List<Product> findBySellerId(String sellerId);

    List<Product> findByCategoryId(Long categoryId);
}