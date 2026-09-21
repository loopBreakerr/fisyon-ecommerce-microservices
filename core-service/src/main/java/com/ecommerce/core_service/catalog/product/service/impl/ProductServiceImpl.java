package com.ecommerce.core_service.catalog.product.service.impl;

import com.ecommerce.core_service.catalog.product.entity.Product;
import com.ecommerce.core_service.catalog.product.entity.ProductImage;
import com.ecommerce.core_service.catalog.product.event.ProductChangedEvent;
import com.ecommerce.core_service.catalog.product.event.ProductEventPublisher;
import com.ecommerce.core_service.catalog.product.model.ProductImageModel;
import com.ecommerce.core_service.catalog.product.model.ProductModel;
import com.ecommerce.core_service.catalog.product.model.ProductQueryModel;
import com.ecommerce.core_service.catalog.product.repository.IProductImageRepository;
import com.ecommerce.core_service.catalog.product.repository.IProductRepository;
import com.ecommerce.core_service.catalog.product.service.IProductService;
import com.ecommerce.core_service.catalog.product.service.ProductViewService;
import com.ecommerce.core_service.inventory.model.InventoryItemModel;
import com.ecommerce.core_service.inventory.service.IInventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProductServiceImpl implements IProductService {

    private final IProductRepository productRepository;
    private final IProductImageRepository productImageRepository;
    private final ProductEventPublisher productEventPublisher;
    private final ProductViewService productViewService;
    private final IInventoryService inventoryService;

    public ProductServiceImpl(IProductRepository productRepository, IProductImageRepository productImageRepository,
                              ProductEventPublisher productEventPublisher, ProductViewService productViewService,
                              IInventoryService inventoryService) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.productEventPublisher = productEventPublisher;
        this.productViewService = productViewService;
        this.inventoryService = inventoryService;
    }

    @Override
    public List<ProductModel> getAllProducts() {
        return toModels(productRepository.findAll());
    }

    @Override
    public List<ProductModel> getProductsByCategory(Long categoryId) {
        return toModels(productRepository.findByCategoryId(categoryId));
    }

    @Override
    public ProductModel getProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow();

        productViewService.incrementView(id);

        return toModel(product, inventoryService.getStockQuantitiesByProductIds(List.of(id)));
    }

    @Override
    public List<ProductModel> getMyProducts(String sellerId) {
        return toModels(productRepository.findBySellerId(sellerId));
    }

    @Override
    public ProductModel createProduct(ProductQueryModel request, String sellerId) {
        if (request.getInitialStock() == null) {
            throw new IllegalArgumentException("Başlangıç stok zorunludur");
        }

        Product product = new Product();
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setSku(generateSku());
        product.setSellerId(sellerId);

        Product saved = productRepository.save(product);

        ProductChangedEvent event = toEvent(saved, "CREATED");
        event.setInitialStock(request.getInitialStock());
        productEventPublisher.publish(event);

        // CatalogEventListener henuz Kafka event'ini islememis olabilir (asenkron) - bu yuzden
        // burada stok bilgisini dogrudan request.getInitialStock()'tan gosteriyoruz, inventory
        // sorgusuna gerek yok (zaten olsa da su an bos donerdi).
        return toModel(saved, Map.of(saved.getId(), request.getInitialStock()));
    }

    @Override
    @Transactional("catalogTransactionManager")
    public ProductModel updateProduct(Long id, ProductQueryModel request, String sellerId) {
        Product product = productRepository.findById(id).orElseThrow();
        checkOwnership(product, sellerId);

        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());

        Product updated = productRepository.save(product);

        productEventPublisher.publish(toEvent(updated, "UPDATED"));

        return toModel(updated, inventoryService.getStockQuantitiesByProductIds(List.of(updated.getId())));
    }

    @Override
    @Transactional("catalogTransactionManager")
    public void deleteProductById(Long id, String sellerId) {
        Product product = productRepository.findById(id).orElseThrow();
        checkOwnership(product, sellerId);
        ProductChangedEvent event = toEvent(product, "DELETED");
        productImageRepository.deleteByProductId(id);
        productRepository.delete(product);
        productEventPublisher.publish(event);
    }

    @Override
    @Transactional("catalogTransactionManager")
    public void deleteProductByUuid(UUID uuid, String sellerId) {
        Product product = productRepository.findByUuid(uuid).orElseThrow();
        checkOwnership(product, sellerId);
        ProductChangedEvent event = toEvent(product, "DELETED");
        productImageRepository.deleteByProductId(product.getId());
        productRepository.delete(product);
        productEventPublisher.publish(event);
    }

    @Override
    public List<ProductModel> getTopViewedProducts(int limit) {
        List<Long> topIds = productViewService.getTopViewedProductIds(limit);

        List<Product> products = new ArrayList<>();
        for (Long id : topIds) {
            productRepository.findById(id).ifPresent(products::add);
        }
        return toModels(products);
    }

    @Override
    @Transactional("catalogTransactionManager")
    public ProductImageModel uploadImage(Long productId, String sellerId, MultipartFile file) throws IOException {
        Product product = productRepository.findById(productId).orElseThrow();
        checkOwnership(product, sellerId);

        productImageRepository.deleteByProductId(productId);

        ProductImage image = new ProductImage();
        image.setProductId(productId);
        image.setImageData(file.getBytes());
        image.setContentType(file.getContentType());
        image.setDisplayOrder(0);

        ProductImage saved = productImageRepository.save(image);

        return toImageModel(saved);
    }

    @Override
    public ProductImage getImageById(Long imageId) {
        return productImageRepository.findById(imageId).orElseThrow();
    }

    @Override
    public InventoryItemModel addStock(Long productId, Integer quantity, String sellerId) {
        Product product = productRepository.findById(productId).orElseThrow();
        checkOwnership(product, sellerId);
        return inventoryService.addStock(productId, quantity);
    }

    private String generateSku() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomSuffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "SKU-" + timestamp + "-" + randomSuffix;
    }

    private void checkOwnership(Product product, String sellerId) {
        if (!product.getSellerId().equals(sellerId)) {
            throw new SecurityException("Bu ürün üzerinde işlem yapma yetkiniz yok");
        }
    }

    private ProductChangedEvent toEvent(Product product, String eventType) {
        ProductChangedEvent event = new ProductChangedEvent();
        event.setProductId(product.getId());
        event.setUuid(product.getUuid().toString());
        event.setName(product.getName());
        event.setDescription(product.getDescription());
        event.setPrice(product.getPrice());
        event.setCategoryId(product.getCategoryId());
        event.setSku(product.getSku());
        event.setIsActive(product.getIsActive());
        event.setSellerId(product.getSellerId());
        event.setEventType(eventType);
        return event;
    }

    private ProductImageModel toImageModel(ProductImage image) {
        return new ProductImageModel(
                image.getId(),
                image.getUuid(),
                image.getProductId(),
                image.getContentType(),
                image.getDisplayOrder()
        );
    }

    // Bir ürün listesini TEK bir toplu inventory sorgusuyla (N+1 yapmadan) ProductModel'e çevirir.
    private List<ProductModel> toModels(List<Product> products) {
        List<Long> productIds = products.stream().map(Product::getId).toList();
        Map<Long, Integer> stockQuantities = inventoryService.getStockQuantitiesByProductIds(productIds);
        return products.stream().map(product -> toModel(product, stockQuantities)).toList();
    }

    private ProductModel toModel(Product product, Map<Long, Integer> stockQuantities) {
        List<ProductImageModel> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId())
                .stream()
                .map(this::toImageModel)
                .toList();

        return new ProductModel(
                product.getId(),
                product.getCategoryId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getSku(),
                product.getIsActive(),
                product.getSellerId(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.getUuid(),
                images,
                stockQuantities.get(product.getId())
        );
    }
}