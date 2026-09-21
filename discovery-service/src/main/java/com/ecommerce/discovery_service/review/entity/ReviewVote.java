package com.ecommerce.discovery_service.review.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "review_votes")
public class ReviewVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "is_helpful")
    private Boolean isHelpful;

    @Column(name = "uuid")
    private UUID uuid;

    public ReviewVote() {
    }

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getIsHelpful() {
        return isHelpful;
    }

    public void setIsHelpful(Boolean isHelpful) {
        this.isHelpful = isHelpful;
    }

    public UUID getUuid() {
        return uuid;
    }
}
