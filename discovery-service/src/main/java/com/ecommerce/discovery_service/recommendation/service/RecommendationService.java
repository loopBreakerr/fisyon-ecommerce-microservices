package com.ecommerce.discovery_service.recommendation.service;

import java.util.List;

public interface RecommendationService {

    void recordInteraction(String userId, Long productId, String interactionType);

    List<Long> getTopRecommendedProductIds(int limit);
}
