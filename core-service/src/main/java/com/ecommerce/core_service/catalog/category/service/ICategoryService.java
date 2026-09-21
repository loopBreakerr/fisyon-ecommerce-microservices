package com.ecommerce.core_service.catalog.category.service;

import com.ecommerce.core_service.catalog.category.model.CategoryQueryModel;
import com.ecommerce.core_service.catalog.category.model.CategoryModel;

import java.util.List;
import java.util.UUID;


public interface ICategoryService {

    List<CategoryModel> getAllCategories();

    CategoryModel getCategoryById(Long id);

    CategoryModel createCategory(CategoryQueryModel request);

    CategoryModel updateCategory(Long id, CategoryQueryModel request);

    void deleteCategoryById(Long id);

    void deleteCategoryByUuid(UUID uuid);
}