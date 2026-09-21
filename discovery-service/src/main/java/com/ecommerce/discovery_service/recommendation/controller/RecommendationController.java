package com.ecommerce.discovery_service.recommendation.controller;

import com.ecommerce.discovery_service.recommendation.model.InteractionRequest;
import com.ecommerce.discovery_service.recommendation.service.RecommendationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping("/recommendations/interactions")
    public void recordInteraction(@RequestBody InteractionRequest request, @AuthenticationPrincipal Jwt jwt) {
        recommendationService.recordInteraction(jwt.getSubject(), request.getProductId(), request.getInteractionType());
    }

    @GetMapping("/recommendations/top")
    public List<Long> getTopRecommendedProductIds(@RequestParam(defaultValue = "10") int limit) {
        return recommendationService.getTopRecommendedProductIds(limit);
    }
}
