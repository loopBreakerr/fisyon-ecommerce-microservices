package com.ecommerce.core_service.admin.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerSummaryModel {

    private final String id;
    private final String username;
    private final String email;
}
