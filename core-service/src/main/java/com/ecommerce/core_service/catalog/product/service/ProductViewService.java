package com.ecommerce.core_service.catalog.product.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ProductViewService {

    private static final String VIEWS_KEY = "product:views";

    private final RedisTemplate<String, String> redisTemplate;

    public ProductViewService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void incrementView(Long productId) {
        redisTemplate.opsForZSet().incrementScore(VIEWS_KEY, String.valueOf(productId), 1);
    }

    public List<Long> getTopViewedProductIds(int limit) {
        Set<String> ids = redisTemplate.opsForZSet().reverseRange(VIEWS_KEY, 0, limit - 1);
        if (ids == null) {
            return List.of();
        }
        return ids.stream().map(Long::valueOf).toList();
    }
}
