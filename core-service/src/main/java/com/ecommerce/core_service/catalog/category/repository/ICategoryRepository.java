package com.ecommerce.core_service.catalog.category.repository;

import com.ecommerce.core_service.catalog.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ICategoryRepository extends JpaRepository<Category,Long> {
    Optional<Category> findByUuid(UUID uuid);
}
