package com.ecommerce.discovery_service.review.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReviewModel {

    private final Long id;
    private final UUID uuid;
    private final String userId;
    private final Long productId;
    private final Integer rating;
    private final String comment;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ReviewModel(Long id, UUID uuid, String userId, Long productId, Integer rating,
                        String comment, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.uuid = uuid;
        this.userId = userId;
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public UUID getUuid() { return uuid; }
    public String getUserId() { return userId; }
    public Long getProductId() { return productId; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
