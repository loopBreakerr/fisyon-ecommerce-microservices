package com.ecommerce.discovery_service.review.service.impl;

import com.ecommerce.discovery_service.review.entity.Review;
import com.ecommerce.discovery_service.review.model.ReviewModel;
import com.ecommerce.discovery_service.review.model.ReviewRequest;
import com.ecommerce.discovery_service.review.repository.ReviewRepository;
import com.ecommerce.discovery_service.review.service.ReviewService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public List<ReviewModel> getReviewsByProductId(Long productId) {
        return reviewRepository.findByProductId(productId)
                .stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public ReviewModel createReview(ReviewRequest request, String userId) {
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("rating 1 ile 5 arasında olmalı");
        }

        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(request.getProductId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review saved = reviewRepository.save(review);
        return toModel(saved);
    }

    private ReviewModel toModel(Review review) {
        return new ReviewModel(
                review.getId(),
                review.getUuid(),
                review.getUserId(),
                review.getProductId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
