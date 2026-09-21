package com.ecommerce.discovery_service.review.repository;

import com.ecommerce.discovery_service.review.entity.ReviewVote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Long> {
}
