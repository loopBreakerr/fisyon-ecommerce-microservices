package com.ecommerce.core_service.catalog.category.service.impl;

import com.ecommerce.core_service.catalog.category.model.CategoryQueryModel;
import com.ecommerce.core_service.catalog.category.model.CategoryModel;
import com.ecommerce.core_service.catalog.category.entity.Category;
import com.ecommerce.core_service.catalog.category.repository.ICategoryRepository;
import com.ecommerce.core_service.catalog.category.service.ICategoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryServiceImpl implements ICategoryService {

    private final ICategoryRepository categoryRepository;

    public CategoryServiceImpl(ICategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryModel> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CategoryModel getCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow();
        return toResponse(category);
    }

    @Override
    public CategoryModel createCategory(CategoryQueryModel request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setParentCategoryId(request.getParentCategoryId());

        Category saved = categoryRepository.save(category);
        return toResponse(saved);
    }

    @Override
    public CategoryModel updateCategory(Long id, CategoryQueryModel request) {
        Category category = categoryRepository.findById(id).orElseThrow();
        category.setName(request.getName());
        category.setParentCategoryId(request.getParentCategoryId());

        Category updated = categoryRepository.save(category);
        return toResponse(updated);
    }

    @Override
    public void deleteCategoryById(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public void deleteCategoryByUuid(UUID uuid) {
        Category category = categoryRepository.findByUuid(uuid).orElseThrow();
        categoryRepository.delete(category);
    }

    private CategoryModel toResponse(Category category) {
        return new CategoryModel(
                category.getId(),
                category.getName(),
                category.getParentCategoryId(),
                category.getUuid()
        );
    }
}