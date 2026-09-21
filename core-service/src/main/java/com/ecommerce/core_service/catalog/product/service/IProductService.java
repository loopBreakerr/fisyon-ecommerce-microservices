package com.ecommerce.core_service.catalog.product.service;

import com.ecommerce.core_service.catalog.product.entity.ProductImage;
import com.ecommerce.core_service.catalog.product.model.ProductImageModel;
import com.ecommerce.core_service.catalog.product.model.ProductModel;
import com.ecommerce.core_service.catalog.product.model.ProductQueryModel;
import com.ecommerce.core_service.inventory.model.InventoryItemModel;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface IProductService {

    List<ProductModel> getAllProducts();

    List<ProductModel> getProductsByCategory(Long categoryId);

    ProductModel getProductById(Long id);

    List<ProductModel> getMyProducts(String sellerId);

    ProductModel createProduct(ProductQueryModel request, String sellerId);

    ProductModel updateProduct(Long id, ProductQueryModel request, String sellerId);

    void deleteProductById(Long id, String sellerId);

    void deleteProductByUuid(UUID uuid, String sellerId);

    List<ProductModel> getTopViewedProducts(int limit);

    ProductImageModel uploadImage(Long productId, String sellerId, MultipartFile file) throws IOException;

    ProductImage getImageById(Long imageId);

    InventoryItemModel addStock(Long productId, Integer quantity, String sellerId);
}