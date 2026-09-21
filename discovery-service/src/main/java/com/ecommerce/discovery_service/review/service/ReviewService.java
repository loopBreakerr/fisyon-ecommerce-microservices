package com.ecommerce.discovery_service.review.service;

import com.ecommerce.discovery_service.review.model.ReviewModel;
import com.ecommerce.discovery_service.review.model.ReviewRequest;

import java.util.List;

public interface ReviewService {

    List<ReviewModel> getReviewsByProductId(Long productId);

    ReviewModel createReview(ReviewRequest request, String userId);
}
