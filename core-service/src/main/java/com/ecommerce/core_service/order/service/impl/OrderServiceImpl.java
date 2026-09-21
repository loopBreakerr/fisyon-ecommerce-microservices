package com.ecommerce.core_service.order.service.impl;

import com.ecommerce.client.internal.cart.ICartClient;
import com.ecommerce.client.internal.catalog.ICatalogClient;
import com.ecommerce.client.internal.catalog.ProductResponse;
import com.ecommerce.core_service.cart.model.CartItemModel;
import com.ecommerce.core_service.cart.model.CartModel;
import com.ecommerce.core_service.cart.service.ICartService;
import com.ecommerce.core_service.order.entity.Order;
import com.ecommerce.core_service.order.entity.OrderItem;
import com.ecommerce.core_service.order.entity.OrderStatusHistory;
import com.ecommerce.core_service.order.event.OrderCreatedEvent;
import com.ecommerce.core_service.order.event.OrderEventPublisher;
import com.ecommerce.core_service.order.model.*;
import com.ecommerce.core_service.order.repository.IOrderItemRepository;
import com.ecommerce.core_service.order.repository.IOrderRepository;
import com.ecommerce.core_service.order.repository.IOrderStatusHistoryRepository;
import com.ecommerce.core_service.order.service.IOrderService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements IOrderService {

    private final IOrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;
    private final IOrderStatusHistoryRepository orderStatusHistoryRepository;
    private final OrderEventPublisher orderEventPublisher;
    private final ICartClient cartClient;
    private final ICartService cartService;
    private final ICatalogClient catalogClient;

    public OrderServiceImpl(IOrderRepository orderRepository,
                            IOrderItemRepository orderItemRepository,
                            IOrderStatusHistoryRepository orderStatusHistoryRepository,
                            OrderEventPublisher orderEventPublisher,
                            ICartClient cartClient, ICartService cartService,
                            ICatalogClient catalogClient) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.orderEventPublisher = orderEventPublisher;
        this.cartClient = cartClient;
        this.cartService = cartService;
        this.catalogClient = catalogClient;
    }

    @Override
    public List<OrderModel> getMyOrders(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public List<OrderModel> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public OrderModel getOrderById(Long id, String userId, boolean isAdmin) {
        Order order = orderRepository.findById(id).orElseThrow();
        if (!isAdmin && !order.getUserId().equals(userId)) {
            throw new SecurityException("Bu sipariş üzerinde işlem yapma yetkiniz yok");
        }
        return toModel(order);
    }

    @Override
    public OrderModel checkout(String userId, PaymentMethod paymentMethod) {
        // a. Sepeti cek
        CartModel cart = cartService.getMyCart(userId);

        // b. Sepet bossa exception firlat
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Sepet boş");
        }

        // c. Order'i olustur, once kaydet ki id uretilsin
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus("PENDING");
        order.setTotalAmount(BigDecimal.ZERO);
        order = orderRepository.save(order);

        // d. Sepetteki her item icin, Catalog'dan gercek fiyati cek ve OrderItem olustur
        List<OrderItem> savedItems = new ArrayList<>();
        for (CartItemModel cartItem : cart.getItems()) {
            ProductResponse product = catalogClient.getProductById(cartItem.getProductId());

            if (!Boolean.TRUE.equals(product.getIsActive())) {
                System.out.println("Product " + cartItem.getProductId() + " is not active, skipping");
                continue;
            }

            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(cartItem.getProductId());
            item.setQuantity(cartItem.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setSellerId(product.getSellerId());
            item.setProductName(product.getName());
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                item.setProductImageId(product.getImages().get(0).getId());
            }
            item = orderItemRepository.save(item);
            savedItems.add(item);
        }

        // e. totalAmount'u hesapla, Order'i guncelle
        BigDecimal totalAmount = savedItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);
        order = orderRepository.save(order);

        // f. OrderStatusHistory kaydi olustur
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrderId(order.getId());
        history.setStatus("PENDING");
        history.setNote("Sipariş sepetten oluşturuldu");
        orderStatusHistoryRepository.save(history);

        // g. Sepeti temizle
        cartClient.clearCart();


        // h. order-events'e OrderCreatedEvent yayinla
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(order.getId());
        event.setUuid(order.getUuid());
        event.setUserId(order.getUserId());
        event.setTotalAmount(order.getTotalAmount());
        event.setStatus(order.getStatus());
        event.setItems(savedItems.stream()
                .map(i -> new OrderCreatedEvent.OrderCreatedEventItem(i.getProductId(), i.getQuantity(), i.getSellerId()))
                .toList());
        event.setPaymentMethod(paymentMethod.name());
        orderEventPublisher.publishOrderCreated(event);

        // i. OrderModel olarak dun
        return toModel(order);
    }

    private OrderModel toModel(Order order) {
        List<OrderItemModel> items = orderItemRepository.findByOrderIdOrderByIdAsc(order.getId())
                .stream()
                .map(item -> new OrderItemModel(
                        item.getId(),
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                        item.getProductName(),
                        item.getProductImageId()
                ))
                .toList();

        return new OrderModel(
                order.getId(),
                order.getUuid(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                items,
                order.getCreatedAt()
        );
    }

    @Override
    public List<SellerOrderItemModel> getOrderItemsBySeller(String sellerId) {
        return orderItemRepository.findBySellerId(sellerId)
                .stream()
                .map(item -> {
                    Order order = orderRepository.findById(item.getOrderId()).orElseThrow();
                    return new SellerOrderItemModel(
                            item.getOrderId(),
                            item.getProductName(),
                            item.getProductImageId(),
                            item.getQuantity(),
                            item.getUnitPrice(),
                            order.getUserId()
                    );
                })
                .toList();
    }
}
