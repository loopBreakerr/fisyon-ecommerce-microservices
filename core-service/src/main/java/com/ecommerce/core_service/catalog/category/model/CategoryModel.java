package com.ecommerce.core_service.catalog.category.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryModel {

    private Long id;
    private String name;
    private Long parentCategoryId;
    private UUID uuid;

}