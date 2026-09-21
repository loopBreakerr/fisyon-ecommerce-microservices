package com.ecommerce.core_service.cart.service.impl;

import com.ecommerce.client.internal.catalog.ICatalogClient;
import com.ecommerce.client.internal.catalog.ProductResponse;
import com.ecommerce.core_service.cart.entity.Cart;
import com.ecommerce.core_service.cart.entity.CartItem;
import com.ecommerce.core_service.cart.model.AddToCartRequest;
import com.ecommerce.core_service.cart.model.CartItemModel;
import com.ecommerce.core_service.cart.model.CartModel;
import com.ecommerce.core_service.cart.repository.ICartItemRepository;
import com.ecommerce.core_service.cart.repository.ICartRepository;
import com.ecommerce.core_service.cart.service.ICartService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements ICartService {

    private final ICartRepository cartRepository;
    private final ICartItemRepository cartItemRepository;
    private final ICatalogClient catalogClient;

    public CartServiceImpl(ICartRepository cartRepository,
                           ICartItemRepository cartItemRepository,
                           ICatalogClient catalogClient) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.catalogClient = catalogClient;
    }

    @Override
    public CartModel getMyCart(String userId) {
        Cart cart = getOrCreateActiveCart(userId);
        return toModel(cart);
    }

    @Override
    public List<CartModel> getAllActiveCarts() {
        return cartRepository.findByStatus("ACTIVE")
                .stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public CartModel addItemToCart(String userId, AddToCartRequest request) {
        Cart cart = getOrCreateActiveCart(userId);

        // Catalog Service'e sorup ürünün gerçekten var olduğunu doğruluyoruz
        ProductResponse product = catalogClient.getProductById(request.getProductId());

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), request.getProductId())
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCartId(cart.getId());
                    newItem.setProductId(request.getProductId());
                    newItem.setQuantity(0);
                    return newItem;
                });

        item.setQuantity(item.getQuantity() + request.getQuantity());
        item.setUnitPriceSnapshot(product.getPrice());
        cartItemRepository.save(item);

        return toModel(cart);
    }

    @Override
    public void removeItemFromCart(String userId, Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Override
    public CartModel updateItemQuantity(String userId, Long cartItemId, int newQuantity) {
        CartItem item = cartItemRepository.findById(cartItemId).orElseThrow();

        if (newQuantity <= 0) {
            removeItemFromCart(userId, cartItemId);
        } else {
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        }

        Cart cart = cartRepository.findById(item.getCartId()).orElseThrow();
        return toModel(cart);
    }

    @Override
    public void clearCart(String userId) {
        Cart cart = getOrCreateActiveCart(userId);
        List<CartItem> items = cartItemRepository.findByCartIdOrderByIdAsc(cart.getId());
        cartItemRepository.deleteAll(items);
    }

    private Cart getOrCreateActiveCart(String userId) {
        return cartRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });
    }

    private CartModel toModel(Cart cart) {
        List<CartItemModel> items = cartItemRepository.findByCartIdOrderByIdAsc(cart.getId())
                .stream()
                .map(item -> {
                    ProductResponse product = catalogClient.getProductById(item.getProductId());
                    BigDecimal lineTotal = item.getUnitPriceSnapshot()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));

                    Long imageId = (product.getImages() != null && !product.getImages().isEmpty())
                            ? product.getImages().get(0).getId()
                            : null;

                    return new CartItemModel(
                            item.getId(),
                            item.getProductId(),
                            product.getName(),
                            imageId,
                            item.getQuantity(),
                            item.getUnitPriceSnapshot(),
                            lineTotal,
                            item.getUuid()
                    );
                })
                .toList();

        BigDecimal totalAmount = items.stream()
                .map(CartItemModel::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartModel(cart.getId(), cart.getUserId(), cart.getStatus(), items, totalAmount, cart.getUuid());
    }
}