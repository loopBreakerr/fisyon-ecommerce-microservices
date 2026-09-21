package com.ecommerce.discovery_service.recommendation.repository;

import com.ecommerce.discovery_service.recommendation.entity.UserInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {

    @Query(value = "SELECT product_id FROM user_interactions "
            + "WHERE interaction_type = 'VIEW' "
            + "GROUP BY product_id "
            + "ORDER BY COUNT(product_id) DESC "
            + "LIMIT :limit", nativeQuery = true)
    List<Long> findTopViewedProductIds(@Param("limit") int limit);
}
