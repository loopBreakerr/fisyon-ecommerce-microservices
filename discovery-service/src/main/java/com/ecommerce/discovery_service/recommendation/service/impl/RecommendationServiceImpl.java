package com.ecommerce.discovery_service.recommendation.service.impl;

import com.ecommerce.discovery_service.recommendation.entity.UserInteraction;
import com.ecommerce.discovery_service.recommendation.repository.UserInteractionRepository;
import com.ecommerce.discovery_service.recommendation.service.RecommendationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private final UserInteractionRepository userInteractionRepository;

    public RecommendationServiceImpl(UserInteractionRepository userInteractionRepository) {
        this.userInteractionRepository = userInteractionRepository;
    }

    @Override
    public void recordInteraction(String userId, Long productId, String interactionType) {
        UserInteraction interaction = new UserInteraction();
        interaction.setUserId(userId);
        interaction.setProductId(productId);
        interaction.setInteractionType(interactionType);
        userInteractionRepository.save(interaction);
    }

    // TODO: bu, en cok VIEW edilen urunleri doneren "kural bazli" basit bir
    // oneri motoru. Ileride kullanici bazli, ML/collaborative-filtering
    // tabanli gercek bir oneri motoruyla degistirilebilir.
    @Override
    public List<Long> getTopRecommendedProductIds(int limit) {
        return userInteractionRepository.findTopViewedProductIds(limit);
    }
}
