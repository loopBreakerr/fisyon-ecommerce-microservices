package com.ecommerce.discovery_service.recommendation.model;

public class InteractionRequest {

    private Long productId;
    private String interactionType;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getInteractionType() { return interactionType; }
    public void setInteractionType(String interactionType) { this.interactionType = interactionType; }
}
