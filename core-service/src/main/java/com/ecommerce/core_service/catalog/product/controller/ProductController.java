package com.ecommerce.core_service.catalog.product.controller;

import com.ecommerce.core_service.catalog.product.entity.ProductImage;
import com.ecommerce.core_service.catalog.product.model.ProductImageModel;
import com.ecommerce.core_service.catalog.product.model.ProductModel;
import com.ecommerce.core_service.catalog.product.model.ProductQueryModel;
import com.ecommerce.core_service.catalog.product.service.IProductService;
import com.ecommerce.core_service.inventory.model.InventoryItemModel;
import com.ecommerce.core_service.inventory.model.StockAdditionRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
public class ProductController {

    private final IProductService productService;

    public ProductController(IProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public List<ProductModel> getAllProducts(@RequestParam(required = false) Long categoryId) {
        if (categoryId != null) {
            return productService.getProductsByCategory(categoryId);
        }
        return productService.getAllProducts();
    }

    @GetMapping("/products/top-viewed")
    public List<ProductModel> getTopViewedProducts(@RequestParam(defaultValue = "10") int limit) {
        return productService.getTopViewedProducts(limit);
    }

    @GetMapping("/products/{id}")
    public ProductModel getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @GetMapping("/products/mine")
    public List<ProductModel> getMyProducts(@AuthenticationPrincipal Jwt jwt) {
        return productService.getMyProducts(jwt.getSubject());
    }

    @PostMapping("/products")
    @PreAuthorize("hasRole('seller') or hasRole('admin')")
    public ProductModel createProduct(@Valid @RequestBody ProductQueryModel request,
                                      @AuthenticationPrincipal Jwt jwt) {
        return productService.createProduct(request, jwt.getSubject());
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasRole('seller') or hasRole('admin')")
    public ProductModel updateProduct(@PathVariable Long id,
                                      @Valid @RequestBody ProductQueryModel request,
                                      @AuthenticationPrincipal Jwt jwt) {
        return productService.updateProduct(id, request, jwt.getSubject());
    }

    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasRole('seller') or hasRole('admin')")
    public void deleteProductById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        productService.deleteProductById(id, jwt.getSubject());
    }

    @DeleteMapping("/products/uuid/{uuid}")
    @PreAuthorize("hasRole('seller') or hasRole('admin')")
    public void deleteProductByUuid(@PathVariable UUID uuid, @AuthenticationPrincipal Jwt jwt) {
        productService.deleteProductByUuid(uuid, jwt.getSubject());
    }

    @PostMapping("/products/{id}/images")
    @PreAuthorize("hasRole('seller') or hasRole('admin')")
    public ProductImageModel uploadImage(@PathVariable Long id,
                                         @RequestParam("file") MultipartFile file,
                                         @AuthenticationPrincipal Jwt jwt) throws IOException {
        return productService.uploadImage(id, jwt.getSubject(), file);
    }

    @PostMapping("/products/{id}/stock")
    @PreAuthorize("hasRole('seller') or hasRole('admin')")
    public InventoryItemModel addStock(@PathVariable Long id,
                                       @Valid @RequestBody StockAdditionRequest request,
                                       @AuthenticationPrincipal Jwt jwt) {
        return productService.addStock(id, request.getQuantity(), jwt.getSubject());
    }

    @GetMapping("/products/images/{imageId}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long imageId) {
        ProductImage image = productService.getImageById(imageId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .body(image.getImageData());
    }
}