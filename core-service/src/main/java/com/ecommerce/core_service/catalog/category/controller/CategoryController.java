package com.ecommerce.core_service.catalog.category.controller;

import com.ecommerce.core_service.catalog.category.model.CategoryQueryModel;
import com.ecommerce.core_service.catalog.category.model.CategoryModel;
import com.ecommerce.core_service.catalog.category.service.ICategoryService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class CategoryController {

    private final ICategoryService categoryService;

    public CategoryController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public List<CategoryModel> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/categories/{id}")
    public CategoryModel getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    @PostMapping("/categories")
    @PreAuthorize("hasRole('seller') or hasRole('admin')")
    public CategoryModel createCategory(@Valid @RequestBody CategoryQueryModel request) {//valid konmasının
        //sebebi categoryquerymodeldaki validation anatasyonlarına bak demesi
        return categoryService.createCategory(request);
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('seller') or hasRole('admin')")
    public CategoryModel updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryQueryModel request) {
        return categoryService.updateCategory(id, request);
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('seller') or hasRole('admin')")
    public void deleteCategoryById(@PathVariable Long id) {
        categoryService.deleteCategoryById(id);
    }

    @DeleteMapping("/categories/uuid/{uuid}")
    @PreAuthorize("hasRole('seller') or hasRole('admin')")
    public void deleteCategoryByUuid(@PathVariable UUID uuid) {
        categoryService.deleteCategoryByUuid(uuid);
    }
}