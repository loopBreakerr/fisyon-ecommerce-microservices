package com.ecommerce.discovery_service.review.controller;

import com.ecommerce.discovery_service.review.model.ReviewModel;
import com.ecommerce.discovery_service.review.model.ReviewRequest;
import com.ecommerce.discovery_service.review.service.ReviewService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/reviews/product/{productId}")
    public List<ReviewModel> getReviewsByProductId(@PathVariable Long productId) {
        return reviewService.getReviewsByProductId(productId);
    }

    @PostMapping("/reviews")
    public ReviewModel createReview(@RequestBody ReviewRequest request, @AuthenticationPrincipal Jwt jwt) {
        return reviewService.createReview(request, jwt.getSubject());
    }
}
